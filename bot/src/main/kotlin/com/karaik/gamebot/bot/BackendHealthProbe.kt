package com.karaik.gamebot.bot

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.Duration

class BackendHealthProbe(
    private val backendBaseUrl: String,
    private val httpClient: OkHttpClient = defaultClient
) {

    fun check(): String {
        val healthUrl = "${backendBaseUrl.trimEnd('/')}/api/health"
        val requestBuilder = Request.Builder()
            .get()
            .url(healthUrl)
        val request = requestBuilder.build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("Backend returned ${response.code}")
            }
            val body = response.body?.string() ?: throw IllegalStateException("Empty backend response")
            val parsed: ApiResponse<String> = mapper.readValue(body)
            return parsed.data ?: "unknown"
        }
    }

    private data class ApiResponse<T>(val code: Int, val message: String?, val data: T?)

    companion object {
        private val mapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())

        private val defaultClient: OkHttpClient = OkHttpClient.Builder()
            .callTimeout(Duration.ofSeconds(10))
            .readTimeout(Duration.ofSeconds(10))
            .build()
    }
}

