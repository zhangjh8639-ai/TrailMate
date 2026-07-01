package com.trailmate.app.feature.routes

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import java.io.ByteArrayOutputStream

sealed interface RouteImportFileReadResult {
    data class Success(
        val fileName: String,
        val content: String,
    ) : RouteImportFileReadResult

    data class Failed(
        val fileName: String,
        val reason: String,
    ) : RouteImportFileReadResult
}

object RouteImportFileReader {
    const val MaxImportBytes: Int = 2 * 1024 * 1024

    private val SpecificRouteMimeTypes = mapOf(
        "application/gpx+xml" to "gpx",
        "application/vnd.google-earth.kml+xml" to "kml",
    )

    fun isSupportedFile(
        fileName: String,
        mimeType: String?,
    ): Boolean {
        val extension = routeExtension(fileName)
        return extension != null ||
            mimeType?.lowercase() in SpecificRouteMimeTypes
    }

    fun displayNameFromPath(path: String?): String =
        path
            ?.substringAfterLast('/')
            ?.takeIf { it.isNotBlank() }
            ?: "未知路线文件"

    fun textFromBytes(
        fileName: String,
        bytes: ByteArray,
    ): RouteImportFileReadResult =
        if (bytes.size > MaxImportBytes) {
            RouteImportFileReadResult.Failed(
                fileName = fileName,
                reason = "文件过大，暂不支持直接导入",
            )
        } else {
            RouteImportFileReadResult.Success(
                fileName = fileName,
                content = bytes.toString(Charsets.UTF_8),
            )
        }

    fun read(
        contentResolver: ContentResolver,
        uri: Uri,
    ): RouteImportFileReadResult {
        val fileName = displayName(contentResolver, uri)
        val mimeType = runCatching {
            contentResolver.getType(uri)
        }.getOrNull()

        if (!isSupportedFile(fileName, mimeType)) {
            return RouteImportFileReadResult.Failed(
                fileName = fileName,
                reason = "文件格式不支持，请选择 GPX 或 KML 文件",
            )
        }

        return runCatching {
            contentResolver.openInputStream(uri)?.use { input ->
                val output = ByteArrayOutputStream()
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var totalBytes = 0
                while (true) {
                    val read = input.read(buffer)
                    if (read == -1) break
                    totalBytes += read
                    if (totalBytes > MaxImportBytes) {
                        return RouteImportFileReadResult.Failed(
                            fileName = fileName,
                            reason = "文件过大，暂不支持直接导入",
                        )
                    }
                    output.write(buffer, 0, read)
                }
                textFromBytes(fileNameForParsing(fileName, mimeType), output.toByteArray())
            } ?: RouteImportFileReadResult.Failed(
                fileName = fileName,
                reason = "无法读取所选文件",
            )
        }.getOrElse {
            RouteImportFileReadResult.Failed(
                fileName = fileName,
                reason = "无法读取所选文件",
            )
        }
    }

    private fun displayName(
        contentResolver: ContentResolver,
        uri: Uri,
    ): String {
        val queriedName = runCatching {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0 && cursor.moveToFirst()) {
                    cursor.getString(index)
                } else {
                    null
                }
            }
        }.getOrNull()

        return queriedName
            ?.takeIf { it.isNotBlank() }
            ?: displayNameFromPath(uri.path)
    }

    internal fun fileNameForParsing(
        fileName: String,
        mimeType: String?,
    ): String {
        if (routeExtension(fileName) != null) {
            return fileName
        }
        val extension = SpecificRouteMimeTypes[mimeType?.lowercase()] ?: return fileName
        return "$fileName.$extension"
    }

    private fun routeExtension(fileName: String): String? {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return extension.takeIf { it == "gpx" || it == "kml" }
    }
}
