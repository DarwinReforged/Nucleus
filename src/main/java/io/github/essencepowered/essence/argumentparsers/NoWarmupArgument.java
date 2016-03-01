/*
 * This file is part of Essence, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.essencepowered.essence.argumentparsers;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.List;

public class NoWarmupArgument extends CommandElement {
    public static final String NO_WARMUP_ARGUMENT = "nowarmup";

    private final Text key;
    private final CommandElement element;

    public NoWarmupArgument(CommandElement element) {
        super(element.getKey());
        this.key = element.getKey();
        this.element = element;
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        return null;
    }

    @Override
    public void parse(CommandSource source, CommandArgs args, CommandContext context) throws ArgumentParseException {
        this.element.parse(source, args, context);

        // We'll get here if there are no exceptions thrown.
        if (key != null && context.getOne(key.toPlain()).isPresent()) {
            context.putArg(NO_WARMUP_ARGUMENT, true);
        }
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return this.element.complete(src, args, context);
    }
}
