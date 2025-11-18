package com.karaik.gamebot.roguelike.theme.skadi;

import com.karaik.gamebot.roguelike.config.RoguelikeThemeRegistry;
import com.karaik.gamebot.roguelike.theme.api.AbstractThemeAnalyzer;
import org.springframework.stereotype.Component;

@Component
public class SkadiThemeAnalyzer extends AbstractThemeAnalyzer {
    public SkadiThemeAnalyzer(RoguelikeThemeRegistry registry) {
        super(registry.getRequired("rogue_4"));
    }
}

