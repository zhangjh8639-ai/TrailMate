package com.trailmate.app.core.network

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.nio.file.Files
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TrailMateHttpPmTilesBasemapFileDownloaderTest {
    private lateinit var server: HttpServer
    private lateinit var downloader: TrailMateHttpPmTilesBasemapFileDownloader

    @Before
    fun setUp() {
        server = HttpServer.create(InetSocketAddress(0), 0)
        server.start()
        downloader = TrailMateHttpPmTilesBasemapFileDownloader()
    }

    @After
    fun tearDown() {
        server.stop(0)
    }

    @Test
    fun downloadsResponseBodyIntoTargetFile() {
        server.respondBytes(
            path = "/offline-basemaps/pmtiles/hangzhou-westlake.pmtiles",
            status = 200,
            bytes = byteArrayOf(1, 2, 3, 4)
        )
        val targetFile = Files.createTempFile("downloaded-basemap", ".pmtiles").toFile()

        val result = downloader.downloadToFile(
            downloadUrl = "http://127.0.0.1:${server.address.port}/offline-basemaps/pmtiles/hangzhou-westlake.pmtiles",
            targetFile = targetFile
        )

        assertEquals(TrailMateApiResult.Success(targetFile), result)
        assertEquals(listOf(1, 2, 3, 4), targetFile.readBytes().map { it.toInt() })
    }

    @Test
    fun returnsFailureForHttpErrorAndLeavesNoPartialFile() {
        server.respondBytes(
            path = "/offline-basemaps/pmtiles/missing.pmtiles",
            status = 404,
            bytes = "missing".toByteArray(Charsets.UTF_8)
        )
        val targetFile = Files.createTempFile("missing-basemap", ".pmtiles").toFile()

        val result = downloader.downloadToFile(
            downloadUrl = "http://127.0.0.1:${server.address.port}/offline-basemaps/pmtiles/missing.pmtiles",
            targetFile = targetFile
        )

        val error = (result as TrailMateApiResult.Failure).error
        assertEquals(404, error.status)
        assertEquals("HTTP_404", error.code)
        assertTrue(!targetFile.exists() || targetFile.length() == 0L)
    }

    private fun HttpServer.respondBytes(
        path: String,
        status: Int,
        bytes: ByteArray
    ) {
        createContext(path) { exchange -> exchange.sendBytes(status, bytes) }
    }

    private fun HttpExchange.sendBytes(status: Int, bytes: ByteArray) {
        responseHeaders.add("Content-Type", "application/octet-stream")
        sendResponseHeaders(status, bytes.size.toLong())
        responseBody.use { output -> output.write(bytes) }
    }
}
