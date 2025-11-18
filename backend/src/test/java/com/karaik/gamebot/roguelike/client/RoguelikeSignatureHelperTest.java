package com.karaik.gamebot.roguelike.client;

import com.karaik.gamebot.roguelike.config.RoguelikeApiProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoguelikeSignatureHelperTest {

    @Test
    void shouldGenerateConsistentSign() {
        RoguelikeApiProperties properties = new RoguelikeApiProperties();
        properties.getApp().setVersionName("1.35.0");
        RoguelikeSignatureHelper helper = new RoguelikeSignatureHelper(properties);

        RoguelikeSignatureHelper.SignatureHeaders headers =
                helper.generateHeaders("/api/test", "{\"foo\":\"bar\"}", 1700000000L, "token");

        assertThat(headers.sign()).isNotBlank();
        assertThat(headers.baseHeaders()).containsEntry("platform", "1");
    }
}
