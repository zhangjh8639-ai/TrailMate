package com.trailmate.app.core.network

import com.trailmate.app.core.map.PmTilesArchiveHeaderParser
import com.trailmate.app.core.map.PmTilesLatLngBounds
import com.trailmate.app.core.map.PmTilesOfflineBasemapImportCandidate
import com.trailmate.app.core.map.PmTilesOfflineBasemapImportPolicy
import java.io.File

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
    private val downloader: TrailMatePmTilesBasemapFileDownloader
) {
    fun importForRoute(
        routeBounds: PmTilesLatLngBounds?,
        routePackKey: String,
        targetDirectory: File
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
        temporaryFile.delete()
        val downloadedFile = when (downloader.downloadToFile(selected.downloadUrl, temporaryFile)) {
            is TrailMateApiResult.Success -> temporaryFile
            is TrailMateApiResult.Failure -> return openLocalPicker(
                "服务端离线地图包获取失败，可选择本地 PMTiles 文件。"
            )
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
}
