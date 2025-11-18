package com.karaik.gamebot.bot

import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.MiraiLogger
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object AknBotLauncher {
    private val logger: MiraiLogger = MiraiLogger.Factory.create(AknBotLauncher::class.java.simpleName)

    @JvmStatic
    fun main(args: Array<String>) {
        val config = BotConfigLoader.load()
        val miraiConfiguration = prepareMiraiConfiguration()
        logger.info("Starting Mirai runtime shell for account ${config.accountId} using protocol ${miraiConfiguration.protocol}.")
        logger.info("Login implementation intentionally skipped; please plug in Mirai login credentials later.")

        startHealthScheduler(config)
        logger.info("Bot started. Press CTRL+C to exit.")
        blockIndefinitely()
    }

    private fun prepareMiraiConfiguration(): BotConfiguration {
        return BotConfiguration().apply {
            protocol = BotConfiguration.MiraiProtocol.ANDROID_PHONE
        }
    }

    private fun startHealthScheduler(config: BotConfig) {
        val scheduler = Executors.newSingleThreadScheduledExecutor { runnable ->
            Thread(runnable, "backend-health-probe").apply { isDaemon = true }
        }
        val probe = BackendHealthProbe(config.sanitizedBackendBaseUrl)
        val interval = config.healthCheckIntervalSeconds
        scheduler.scheduleAtFixedRate({
            try {
                val result = probe.check()
                logger.info("Backend health check response: $result")
            } catch (ex: Exception) {
                logger.warning("Backend health check failed: ${ex.message}")
            }
        }, 0, interval, TimeUnit.SECONDS)

        Runtime.getRuntime().addShutdownHook(Thread {
            logger.info("Shutting down bot scheduler.")
            scheduler.shutdownNow()
        })
    }

    private fun blockIndefinitely() {
        while (!Thread.currentThread().isInterrupted) {
            try {
                Thread.sleep(Duration.ofMinutes(5).toMillis())
            } catch (ex: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
    }
}

