package com.karaik.gamebot.bot

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue

object BotConfigLoader {
    private val mapper: ObjectMapper = ObjectMapper(YAMLFactory()).registerModule(KotlinModule.Builder().build())

    fun load(resourcePath: String = "bot-config.yml"): BotConfig {
        val inputStream = BotConfigLoader::class.java.classLoader.getResourceAsStream(resourcePath)
            ?: throw IllegalStateException("Missing bot configuration file: $resourcePath")

        return inputStream.use { mapper.readValue(it) }
    }
}

