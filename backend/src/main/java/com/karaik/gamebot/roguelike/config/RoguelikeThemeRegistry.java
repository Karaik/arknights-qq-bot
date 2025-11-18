package com.karaik.gamebot.roguelike.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class RoguelikeThemeRegistry {

    private final Map<String, RoguelikeThemeConfig> configsById;
    private final Map<String, RoguelikeThemeConfig> configsByName;

    public RoguelikeThemeRegistry(ObjectMapper objectMapper) {
        this.configsById = new HashMap<>();
        this.configsByName = new HashMap<>();
        loadConfigs(objectMapper);
    }

    private void loadConfigs(ObjectMapper mapper) {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = resolver.getResources("classpath*:roguelike/themes/*.json");
            for (Resource resource : resources) {
                RoguelikeThemeConfig config = mapper.readValue(resource.getInputStream(), RoguelikeThemeConfig.class);
                configsById.put(config.getThemeId(), config);
                configsByName.put(config.getName(), config);
            }
        } catch (IOException e) {
            throw new RoguelikeConfigException("Failed to load theme configuration", e);
        }
    }

    public Optional<RoguelikeThemeConfig> findByIdOrName(String idOrName) {
        if (idOrName == null) {
            return Optional.empty();
        }
        RoguelikeThemeConfig config = configsById.get(idOrName);
        if (config == null) {
            config = configsByName.get(idOrName);
        }
        return Optional.ofNullable(config);
    }

    public RoguelikeThemeConfig getRequired(String idOrName) {
        return findByIdOrName(idOrName)
                .orElseThrow(() -> new RoguelikeConfigException("Theme configuration not found: " + idOrName));
    }

    public Collection<RoguelikeThemeConfig> listThemes() {
        return Collections.unmodifiableCollection(configsById.values());
    }
}

