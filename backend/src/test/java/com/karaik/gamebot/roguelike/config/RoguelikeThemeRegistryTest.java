package com.karaik.gamebot.roguelike.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RoguelikeThemeRegistryTest {

    @Autowired
    private RoguelikeThemeRegistry registry;

    @Test
    void shouldLoadThemeFromResources() {
        assertThat(registry.listThemes()).isNotEmpty();
        RoguelikeThemeConfig config = registry.getRequired("rogue_4");
        assertThat(config.getName()).isEqualTo("萨卡兹的无终奇语");
    }
}

