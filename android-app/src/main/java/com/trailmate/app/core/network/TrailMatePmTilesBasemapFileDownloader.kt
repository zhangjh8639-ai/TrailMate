package com.trailmate.app.core.network

import java.io.File
import java.net.HttpURLConnection
import java.net.URL

interface TrailMatePmTilesBasemapFileDownloader {
    fun downloadToFile(
        downloadUrl: String,
        targetFile: File
    ): TrailMateApiResult<File>
}

class TrailMateHttpPmTilesBasemapFileDownloader(
    private val connectTimeoutMillis: Int = 10_000,
    private val readTimeoutMillis: Int = 30_000
) : TrailMatePmTilesBasemapFileDownloader {
    override fun downloadToFile(
        downloadUrl: String,
        targetFile: File
    ): TrailMateApiResult<File> {
        val connection = URL(downloadUrl).openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = connectTimeoutMillis
            connection.readTimeout = readTimeoutMillis
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                targetFile.delete()
                return TrailMateApiResult.Failure(
                    TrailMateApiError(
                        status = responseCode,
                        code = "HTTP_$responseCode",
                        message = "PMTiles download failed.",
                        traceId = null
                    )
                )
            }

            targetFile.parentFile?.mkdirs()
            connection.inputStream.use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            TrailMateApiResult.Success(targetFile)
        } catch (exception: Exception) {
            targetFile.delete()
            TrailMateApiResult.Failure(
                TrailMateApiError(
                    status = 0,
                    code = "NETWORK_ERROR",
                    message = exception.message ?: "PMTiles download failed.",
                    traceId = null
                )
            )
        } finally {
            connection.disconnect()
        }
    }
}
