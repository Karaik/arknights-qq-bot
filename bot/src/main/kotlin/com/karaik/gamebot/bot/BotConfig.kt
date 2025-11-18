package com.karaik.gamebot.bot

data class BotConfig(
    val accountId: Long,
    val backendBaseUrl: String,
    val healthCheckIntervalSeconds: Long = 30,
    val backendToken: String? = null
) {
    init {
        require(backendBaseUrl.isNotBlank()) { "backendBaseUrl must not be blank" }
        require(healthCheckIntervalSeconds >= 5) { "healthCheckIntervalSeconds should be at least 5 seconds" }
    }

    val sanitizedBackendBaseUrl: String = backendBaseUrl.trimEnd('/')
}

