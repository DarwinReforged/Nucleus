/*
 * This file is part of Essence, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.commands.teleport;

import io.github.essencepowered.essence.Util;
import io.github.essencepowered.essence.api.PluginModule;
import io.github.essencepowered.essence.argumentparsers.NoCostArgument;
import io.github.essencepowered.essence.argumentparsers.NoWarmupArgument;
import io.github.essencepowered.essence.argumentparsers.TwoPlayersArgument;
import io.github.essencepowered.essence.internal.CommandBase;
import io.github.essencepowered.essence.internal.ConfigMap;
import io.github.essencepowered.essence.internal.annotations.ConfigCommandAlias;
import io.github.essencepowered.essence.internal.annotations.Modules;
import io.github.essencepowered.essence.internal.annotations.Permissions;
import io.github.essencepowered.essence.internal.annotations.RegisterCommand;
import io.github.essencepowered.essence.internal.permissions.PermissionInformation;
import io.github.essencepowered.essence.internal.permissions.SuggestedLevel;
import io.github.essencepowered.essence.internal.services.TeleportHandler;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Permissions(root = "teleport", alias = "teleport", suggestedLevel = SuggestedLevel.MOD)
@Modules(PluginModule.TELEPORT)
@RegisterCommand({})
@ConfigCommandAlias("teleport")
public class TeleportCommand extends CommandBase<CommandSource> {

    private String playerFromKey = "playerFrom";
    private String playerKey = "player";

    @Override
    public Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
            m.put("others", new PermissionInformation("permission.teleport.others", SuggestedLevel.ADMIN));
        return m;
    }

    @Override
    public CommandSpec createSpec() {
        return CommandSpec.builder().executor(this).arguments(
            GenericArguments.flags().flag("f").buildWith(GenericArguments.none()),

            // Either we get two arguments, or we get one.
            GenericArguments.firstParsing(
                // <player> <player>
                new NoCostArgument(new NoWarmupArgument(new TwoPlayersArgument(Text.of(playerFromKey), Text.of(playerKey), permissions.getPermissionWithSuffix("others")))),

                // <player>
                GenericArguments.onlyOne(GenericArguments.player(Text.of(playerKey)))
            )
        ).build();
    }

    @Override
    public ContinueMode preProcessChecks(CommandSource source, CommandContext args) throws Exception {
        // Do the /tptoggle check now, no need to go through a warmup then...
        if (source instanceof Player && !TeleportHandler.canBypassTpToggle((Player) source)) {
            Player to = args.<Player>getOne(playerKey).get();
            if (!plugin.getUserLoader().getUser(to).isTeleportToggled()) {
                source.sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("teleport.fail.targettoggle", to.getName())));
                return ContinueMode.STOP;
            }
        }

        return ContinueMode.CONTINUE;
    }

    @Override
    public String[] getAliases() {
        if (aliases == null) {
            // Some people want /tp to be held by minecraft. This will allow us to do so.
            if (plugin.getConfig(ConfigMap.COMMANDS_CONFIG).get().getCommandNode("teleport").getNode("use-tp-command").getBoolean(true)) {
                aliases = new String[] { "tp", "teleport" };
            } else {
                aliases = new String[] { "teleport" };
            }
        }

        return super.getAliases();
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Optional<Player> ofrom = args.<Player>getOne(playerFromKey);
        Player from;
        if (ofrom.isPresent()) {
            from = ofrom.get();
            if (from.equals(src)) {
                src.sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("command.teleport.player.noself")));
                return CommandResult.empty();
            }
        } else if (src instanceof Player) {
            from = (Player)src;
        } else {
            src.sendMessage(Text.of(TextColors.RED, Util.getMessageWithFormat("command.playeronly")));
            return CommandResult.empty();
        }

        Player pl = args.<Player>getOne(playerKey).get();
        if (plugin.getTpHandler().getBuilder().setSource(src).setFrom(from).setTo(pl).setSafe(!args.<Boolean>getOne("f").orElse(false)).startTeleport()) {
            return CommandResult.success();
        }

        return CommandResult.empty();
    }

    @Override
    public CommentedConfigurationNode getDefaults() {
        CommentedConfigurationNode ccn = super.getDefaults();
        ccn.getNode("use-tp-command").setComment(Util.getMessageWithFormat("config.command.teleport.tp")).setValue(true);
        return ccn;
    }
}
