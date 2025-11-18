package com.karaik.gamebot.bot

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BackendHealthProbeTest {

    private lateinit var server: MockWebServer

    @BeforeTest
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @AfterTest
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `should parse successful health response`() {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"code":0,"message":"ok","data":"ok"}""")
                .addHeader("Content-Type", "application/json")
        )

        val url = server.url("/").toString().removeSuffix("/")
        val status = BackendHealthProbe(url).check()

        assertEquals("ok", status)
    }
}

