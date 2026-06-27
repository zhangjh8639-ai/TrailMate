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

    @Test
    fun deletesExistingPartialFileForPermanentClientError() {
        server.respondBytes(
            path = "/offline-basemaps/pmtiles/gone.pmtiles",
            status = 404,
            bytes = "gone".toByteArray(Charsets.UTF_8)
        )
        val targetFile = Files.createTempFile("gone-basemap", ".pmtiles").toFile().apply {
            writeBytes(byteArrayOf(1, 2))
        }

        val result = downloader.downloadToFile(
            downloadUrl = "http://127.0.0.1:${server.address.port}/offline-basemaps/pmtiles/gone.pmtiles",
            targetFile = targetFile
        )

        val error = (result as TrailMateApiResult.Failure).error
        assertEquals(404, error.status)
        assertEquals("HTTP_404", error.code)
        assertTrue(!targetFile.exists())
    }

    @Test
    fun preservesExistingPartialFileWhenNetworkRequestFails() {
        val targetFile = Files.createTempFile("network-error-basemap", ".pmtiles").toFile().apply {
            writeBytes(byteArrayOf(1, 2))
        }

        val result = downloader.downloadToFile(
            downloadUrl = "http://127.0.0.1:1/offline-basemaps/pmtiles/retry.pmtiles",
            targetFile = targetFile
        )

        val error = (result as TrailMateApiResult.Failure).error
        assertEquals(0, error.status)
        assertEquals("NETWORK_ERROR", error.code)
        assertEquals(listOf(1, 2), targetFile.readBytes().map { it.toInt() })
    }

    @Test
    fun sendsBearerTokenWhenProvided() {
        var authorizationHeader: String? = null
        server.createContext("/offline-basemaps/pmtiles/private.pmtiles") { exchange ->
            authorizationHeader = exchange.requestHeaders.getFirst("Authorization")
            exchange.sendBytes(200, byteArrayOf(7, 8))
        }
        val targetFile = Files.createTempFile("private-basemap", ".pmtiles").toFile()

        val result = downloader.downloadToFile(
            downloadUrl = "http://127.0.0.1:${server.address.port}/offline-basemaps/pmtiles/private.pmtiles",
            targetFile = targetFile,
            authorizationBearerToken = "access_token"
        )

        assertEquals(TrailMateApiResult.Success(targetFile), result)
        assertEquals("Bearer access_token", authorizationHeader)
        assertEquals(listOf(7, 8), targetFile.readBytes().map { it.toInt() })
    }

    @Test
    fun resumesExistingPartialFileWithRangeAndAppend() {
        var rangeHeader: String? = null
        server.createContext("/offline-basemaps/pmtiles/resumable.pmtiles") { exchange ->
            rangeHeader = exchange.requestHeaders.getFirst("Range")
            exchange.responseHeaders.add("Content-Range", "bytes 2-3/4")
            exchange.sendBytes(206, byteArrayOf(3, 4))
        }
        val targetFile = Files.createTempFile("resumable-basemap", ".pmtiles").toFile().apply {
            writeBytes(byteArrayOf(1, 2))
        }

        val result = downloader.downloadToFile(
            downloadUrl = "http://127.0.0.1:${server.address.port}/offline-basemaps/pmtiles/resumable.pmtiles",
            targetFile = targetFile
        )

        assertEquals(TrailMateApiResult.Success(targetFile), result)
        assertEquals("bytes=2-", rangeHeader)
        assertEquals(listOf(1, 2, 3, 4), targetFile.readBytes().map { it.toInt() })
    }

    @Test
    fun rejectsMismatchedPartialContentRangeWithoutCorruptingPartialFile() {
        server.createContext("/offline-basemaps/pmtiles/mismatched-range.pmtiles") { exchange ->
            exchange.responseHeaders.add("Content-Range", "bytes 0-3/4")
            exchange.sendBytes(206, byteArrayOf(1, 2, 3, 4))
        }
        val targetFile = Files.createTempFile("mismatched-range-basemap", ".pmtiles").toFile().apply {
            writeBytes(byteArrayOf(1, 2))
        }

        val result = downloader.downloadToFile(
            downloadUrl = "http://127.0.0.1:${server.address.port}/offline-basemaps/pmtiles/mismatched-range.pmtiles",
            targetFile = targetFile
        )

        val error = (result as TrailMateApiResult.Failure).error
        assertEquals(206, error.status)
        assertEquals("PMTILES_RANGE_MISMATCH", error.code)
        assertEquals(listOf(1, 2), targetFile.readBytes().map { it.toInt() })
    }

    @Test
    fun overwritesPartialFileWhenServerIgnoresRange() {
        var rangeHeader: String? = null
        server.createContext("/offline-basemaps/pmtiles/no-range.pmtiles") { exchange ->
            rangeHeader = exchange.requestHeaders.getFirst("Range")
            exchange.sendBytes(200, byteArrayOf(9, 10, 11))
        }
        val targetFile = Files.createTempFile("restart-basemap", ".pmtiles").toFile().apply {
            writeBytes(byteArrayOf(1, 2))
        }

        val result = downloader.downloadToFile(
            downloadUrl = "http://127.0.0.1:${server.address.port}/offline-basemaps/pmtiles/no-range.pmtiles",
            targetFile = targetFile
        )

        assertEquals(TrailMateApiResult.Success(targetFile), result)
        assertEquals("bytes=2-", rangeHeader)
        assertEquals(listOf(9, 10, 11), targetFile.readBytes().map { it.toInt() })
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
