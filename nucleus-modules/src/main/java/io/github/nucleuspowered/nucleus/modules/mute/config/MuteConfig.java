/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.mute.config;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.configurate.config.CommonPermissionLevelConfig;
import io.github.nucleuspowered.nucleus.services.interfaces.annotation.configuratehelper.LocalisedComment;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.List;

@ConfigSerializable
public class MuteConfig {

    @Setting(value = "blocked-commands")
    @LocalisedComment("config.mute.blocked")
    private List<String> blockedCommands = Lists.newArrayList("minecraft:me", "say");

    @Setting(value = "maximum-mute-length")
    @LocalisedComment("config.mute.maxmutelength")
    private long maxMuteLength = 604800;

    @Setting(value = "see-muted-chat")
    @LocalisedComment("config.mute.seemutedchat")
    private boolean showMutedChat = false;

    @Setting(value = "muted-chat-tag")
    @LocalisedComment("config.mute.seemutedchattag")
    private String cancelledTag = "&c[cancelled] ";

    @Setting(value = "mute-time-counts-online-only")
    @LocalisedComment("config.mute.countonlineonly")
    private boolean muteOnlineOnly = false;

    @Setting(value = "require-separate-unmute-permission")
    @LocalisedComment("config.mute.unmute")
    private boolean requireUnmutePermission = false;

    @Setting(value = "mute-permission-levels")
    @LocalisedComment("config.mute.permissionlevel")
    private CommonPermissionLevelConfig level = new CommonPermissionLevelConfig();

    public List<String> getBlockedCommands() {
        return this.blockedCommands;
    }

    public long getMaximumMuteLength() {
        return this.maxMuteLength;
    }

    public boolean isShowMutedChat() {
        return this.showMutedChat;
    }

    public String getCancelledTag() {
        return this.cancelledTag;
    }

    public boolean isMuteOnlineOnly() {
        return this.muteOnlineOnly;
    }

    public CommonPermissionLevelConfig getLevelConfig() {
        return this.level;
    }
}
