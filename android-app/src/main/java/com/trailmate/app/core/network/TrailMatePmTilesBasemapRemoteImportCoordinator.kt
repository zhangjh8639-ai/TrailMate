package com.trailmate.app.core.network

import com.trailmate.app.core.map.PmTilesArchiveHeaderParser
import com.trailmate.app.core.map.PmTilesLatLngBounds
import com.trailmate.app.core.map.PmTilesOfflineBasemapImportCandidate
import com.trailmate.app.core.map.PmTilesOfflineBasemapImportPolicy
import java.io.File
import java.security.MessageDigest

enum class TrailMatePmTilesRemoteImportAction {
    IMPORTED,
    OPEN_LOCAL_PICKER
}

data class TrailMatePmTilesRemoteImportResult(
    val action: TrailMatePmTilesRemoteImportAction,
    val message: String
)

class TrailMatePmTilesBasemapRemoteImportCoordinator(
    private val catalogApi: TrailMateOfflineBasemapCatalogApi?,
    private val downloader: TrailMatePmTilesBasemapFileDownloader,
    private val usableStorageBytes: (File) -> Long = { directory -> directory.usableSpace }
) {
    fun importForRoute(
        routeBounds: PmTilesLatLngBounds?,
        routePackKey: String,
        targetDirectory: File,
        authorizationBearerToken: String? = null
    ): TrailMatePmTilesRemoteImportResult {
        if (routeBounds == null || catalogApi == null) {
            return openLocalPicker("请选择本地 PMTiles 离线地图包。")
        }

        val catalog = when (val result = catalogApi.listPmTilesBasemaps(routeBounds)) {
            is TrailMateApiResult.Success -> result.value
            is TrailMateApiResult.Failure -> return openLocalPicker(
                "暂时无法连接服务端地图包目录，可选择本地 PMTiles 文件。"
            )
        }
        val selected = TrailMateOfflineBasemapCatalogSelectionPolicy.selectForRoute(
            routeBounds = routeBounds,
            catalog = catalog
        ) ?: return openLocalPicker(
            "未找到适配当前路线的服务端地图包，可选择本地 PMTiles 文件。"
        )

        targetDirectory.mkdirs()
        val temporaryFile = targetDirectory.resolve("$routePackKey.pmtiles.download")
        if (!hasEnoughStorageForDownload(selected.sizeBytes, temporaryFile, targetDirectory)) {
            return openLocalPicker(
                "手机剩余空间不足，无法下载服务端离线地图包；请清理空间或选择本地 PMTiles 文件。"
            )
        }
        val downloadedFile = when (downloader.downloadToFile(
            downloadUrl = selected.downloadUrl,
            targetFile = temporaryFile,
            authorizationBearerToken = authorizationBearerToken
        )) {
            is TrailMateApiResult.Success -> temporaryFile
            is TrailMateApiResult.Failure -> return openLocalPicker(
                "服务端离线地图包获取失败，可选择本地 PMTiles 文件。"
            )
        }
        if (!downloadedFile.matchesExpectedSize(selected.sizeBytes)) {
            temporaryFile.delete()
            return openLocalPicker("服务端离线地图包大小校验未通过，可选择本地 PMTiles 文件。")
        }
        if (!downloadedFile.matchesExpectedSha256(selected.sha256)) {
            temporaryFile.delete()
            return openLocalPicker("服务端离线地图包完整性校验未通过，可选择本地 PMTiles 文件。")
        }
        val decision = PmTilesOfflineBasemapImportPolicy.resolve(
            candidate = PmTilesOfflineBasemapImportCandidate(
                displayName = "${selected.packId}.pmtiles",
                sizeBytes = downloadedFile.length(),
                archiveInspection = PmTilesArchiveHeaderParser.inspect(downloadedFile)
            ),
            routePackKey = routePackKey,
            targetBounds = routeBounds
        )
        if (!decision.canImport) {
            temporaryFile.delete()
            return openLocalPicker("服务端离线地图包校验未通过，可选择本地 PMTiles 文件。")
        }

        val targetFile = targetDirectory.resolve(decision.targetFileName)
        targetFile.delete()
        val moved = downloadedFile.renameTo(targetFile) || runCatching {
            downloadedFile.copyTo(targetFile, overwrite = true)
            downloadedFile.delete()
            true
        }.getOrDefault(false)
        if (!moved || !targetFile.isFile || targetFile.length() <= 0L) {
            targetFile.delete()
            temporaryFile.delete()
            return openLocalPicker("服务端离线地图包保存失败，可选择本地 PMTiles 文件。")
        }

        return TrailMatePmTilesRemoteImportResult(
            action = TrailMatePmTilesRemoteImportAction.IMPORTED,
            message = "已导入${selected.regionName}离线地图包，正在刷新地图。"
        )
    }

    private fun openLocalPicker(message: String): TrailMatePmTilesRemoteImportResult =
        TrailMatePmTilesRemoteImportResult(
            action = TrailMatePmTilesRemoteImportAction.OPEN_LOCAL_PICKER,
            message = message
        )

    private fun File.matchesExpectedSize(expectedSizeBytes: Long?): Boolean {
        val expected = expectedSizeBytes?.takeIf { it > 0L } ?: return true
        return length() == expected
    }

    private fun hasEnoughStorageForDownload(
        expectedSizeBytes: Long?,
        temporaryFile: File,
        targetDirectory: File
    ): Boolean {
        val expected = expectedSizeBytes?.takeIf { it > 0L } ?: return true
        val existing = if (temporaryFile.isFile) temporaryFile.length().coerceAtLeast(0L) else 0L
        val remaining = (expected - existing).coerceAtLeast(0L)
        return remaining <= usableStorageBytes(targetDirectory).coerceAtLeast(0L)
    }

    private fun File.matchesExpectedSha256(expectedSha256: String?): Boolean {
        val expected = expectedSha256?.trim()?.lowercase()?.takeIf { it.isNotEmpty() } ?: return true
        if (!expected.matches(SHA_256_HEX_PATTERN)) {
            return false
        }
        return sha256Hex() == expected
    }

    private fun File.sha256Hex(): String {
        val digest = MessageDigest.getInstance("SHA-256")
        inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                if (count > 0) {
                    digest.update(buffer, 0, count)
                }
            }
        }
        return digest.digest().joinToString(separator = "") { byte -> "%02x".format(byte) }
    }

    private companion object {
        val SHA_256_HEX_PATTERN = Regex("^[0-9a-f]{64}$")
    }
}
