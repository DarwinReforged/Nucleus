/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.chat.services;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.modules.chat.config.ChatConfig;
import io.github.nucleuspowered.nucleus.modules.chat.config.ChatTemplateConfig;
import io.github.nucleuspowered.nucleus.modules.chat.config.WeightedChatTemplateConfig;
import io.github.nucleuspowered.nucleus.scaffold.service.ServiceBase;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.services.interfaces.IReloadableService;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.context.Contextual;
import org.spongepowered.api.service.permission.Subject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.inject.Inject;

/**
 * Contains the logic for caching templates and the template selection logic.
 */
public class ChatService implements IReloadableService.Reloadable, ServiceBase {

    private final INucleusServiceCollection serviceCollection;
    private final AtomicBoolean currentlyReloading = new AtomicBoolean(false);
    private LinkedHashMap<String, WeightedChatTemplateConfig> cachedTemplates = null;
    private ChatConfig config = new ChatConfig();

    @Inject
    public ChatService(INucleusServiceCollection serviceCollection) {
        this.serviceCollection = serviceCollection;
    }

    public ChatTemplateConfig getDefaultTemplate() {
        return this.config.getDefaultTemplate();
    }

    public ChatTemplateConfig getTemplateNow(Subject subject) {
        if (!this.config.isUseGroupTemplates()) {
            return this.config.getDefaultTemplate();
        }

        Optional<String> groupString = subject.getOption("nucleus.chat.group");
        List<String> groups = new ArrayList<>();
        if (groupString.isPresent()) {
            groups.add(groupString.get());
        } else if (this.config.isCheckPermissionGroups()) {
            // Expensive, should hide behind a switch.
            try {
                groups = Util.getParentSubjects(subject)
                    .get(100, TimeUnit.MILLISECONDS)
                    .stream()
                    .map(Contextual::getIdentifier)
                    .collect(Collectors.toList());
            } catch (Exception e) {
                this.serviceCollection.logger().error(
                        this.serviceCollection.messageProvider().getMessageString("chat.templates.timeout", subject.getIdentifier())
                );
                return this.config.getDefaultTemplate();
            }

            if (groups == null || groups.isEmpty()) {
                return this.config.getDefaultTemplate();
            }
        } else {
            // Nothin'. Return
            return this.config.getDefaultTemplate();
        }

        // For each weight...
        for (Map.Entry<String, WeightedChatTemplateConfig> templates : this.cachedTemplates.entrySet()) {
            // Iterate through all groups the subject is in.
            for (String group : groups) {
                if (templates.getKey().equalsIgnoreCase(group)) {
                    return templates.getValue();
                }
            }
        }

        // Return the default.
        return this.config.getDefaultTemplate();
    }

    @Override
    public void onReload(INucleusServiceCollection serviceCollection) {
        try {
            this.config = serviceCollection.moduleDataProvider().getModuleConfig(ChatConfig.class);
            if (!this.currentlyReloading.get()) {
                this.currentlyReloading.set(true);
                // Do this off the main thread to not cause a lockup
                Task.builder().async().execute(() -> {
                    try {
                        if (this.config.isUseGroupTemplates()) {
                            LinkedHashMap<String, WeightedChatTemplateConfig> sw = new LinkedHashMap<>();
                            SortedMap<Integer, Set<Map.Entry<String, WeightedChatTemplateConfig>>> firstStage =
                                    new TreeMap<>(Comparator.reverseOrder());
                            for (Map.Entry<String, WeightedChatTemplateConfig> me : this.config.getGroupTemplates().entrySet()) {
                                // For each weight, get the set.
                                Set<Map.Entry<String, WeightedChatTemplateConfig>> sme = firstStage
                                        .computeIfAbsent(me.getValue().getWeight(), s -> new HashSet<>());

                                sme.add(me);
                            }

                            // keySet is in order.
                            for (int i : firstStage.keySet()) {
                                firstStage.get(i).forEach(x -> sw.put(x.getKey(), x.getValue()));
                            }

                            this.cachedTemplates = sw;
                        } else {
                            this.cachedTemplates = new LinkedHashMap<>();
                        }
                    } finally {
                        this.currentlyReloading.set(false);
                    }
                }).submit(serviceCollection.pluginContainer());

            }
        } catch (Exception e) {
            this.currentlyReloading.set(false);
        }
    }
}
