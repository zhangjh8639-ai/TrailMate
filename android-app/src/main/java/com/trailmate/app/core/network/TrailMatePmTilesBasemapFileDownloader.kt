package com.trailmate.app.core.network

import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

interface TrailMatePmTilesBasemapFileDownloader {
    fun downloadToFile(
        downloadUrl: String,
        targetFile: File,
        authorizationBearerToken: String? = null
    ): TrailMateApiResult<File>
}

class TrailMateHttpPmTilesBasemapFileDownloader(
    private val connectTimeoutMillis: Int = 10_000,
    private val readTimeoutMillis: Int = 30_000
) : TrailMatePmTilesBasemapFileDownloader {
    override fun downloadToFile(
        downloadUrl: String,
        targetFile: File,
        authorizationBearerToken: String?
    ): TrailMateApiResult<File> {
        val connection = URL(downloadUrl).openConnection() as HttpURLConnection
        val existingBytes = if (targetFile.isFile) targetFile.length() else 0L
        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = connectTimeoutMillis
            connection.readTimeout = readTimeoutMillis
            connection.setRequestProperty("Accept", "application/octet-stream")
            authorizationBearerToken?.trim()?.takeIf { it.isNotEmpty() }?.let { token ->
                connection.setRequestProperty("Authorization", "Bearer $token")
            }
            if (existingBytes > 0L) {
                connection.setRequestProperty("Range", "bytes=$existingBytes-")
            }
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                if (responseCode in 400..499 && responseCode != 408 && responseCode != 429) {
                    targetFile.delete()
                }
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
            val append = responseCode == HttpURLConnection.HTTP_PARTIAL && existingBytes > 0L
            if (append && connection.partialContentRangeStart() != existingBytes) {
                return TrailMateApiResult.Failure(
                    TrailMateApiError(
                        status = responseCode,
                        code = "PMTILES_RANGE_MISMATCH",
                        message = "PMTiles partial download range did not match the existing file.",
                        traceId = null
                    )
                )
            }
            connection.inputStream.use { input ->
                FileOutputStream(targetFile, append).use { output ->
                    input.copyTo(output)
                }
            }
            TrailMateApiResult.Success(targetFile)
        } catch (exception: Exception) {
            if (!targetFile.isFile || targetFile.length() <= 0L) {
                targetFile.delete()
            }
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

    private fun HttpURLConnection.partialContentRangeStart(): Long? {
        val header = getHeaderField("Content-Range")?.trim() ?: return null
        val match = CONTENT_RANGE_PATTERN.matchEntire(header) ?: return null
        return match.groupValues[1].toLongOrNull()
    }

    private companion object {
        val CONTENT_RANGE_PATTERN = Regex("""bytes\s+(\d+)-\d+/\d+""")
    }
}
