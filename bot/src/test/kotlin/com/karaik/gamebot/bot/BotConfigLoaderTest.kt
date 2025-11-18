package com.karaik.gamebot.bot

import kotlin.test.Test
import kotlin.test.assertTrue

class BotConfigLoaderTest {

    @Test
    fun `should load default config`() {
        val config = BotConfigLoader.load()

        assertTrue(config.backendBaseUrl.isNotBlank(), "backend base url should not be blank")
        assertTrue(config.healthCheckIntervalSeconds >= 5, "health interval should respect lower bound")
    }
}

