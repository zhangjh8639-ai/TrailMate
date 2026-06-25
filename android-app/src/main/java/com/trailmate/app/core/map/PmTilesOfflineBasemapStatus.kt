package com.trailmate.app.core.map

import java.io.File

enum class PmTilesOfflineBasemapStatus {
    READY,
    MISSING_FILE,
    INVALID_FORMAT,
    UNSUPPORTED_TILE_TYPE,
    EMPTY_FILE,
    REGION_NOT_COVERED
}

data class PmTilesOfflineBasemapManifest(
    val fileName: String,
    val fileExists: Boolean,
    val fileSizeBytes: Long,
    val coveredRegionNames: Set<String> = emptySet(),
    val archiveHeader: PmTilesArchiveHeader? = null,
    val archiveHeaderError: PmTilesArchiveHeaderError? = null,
    val targetBounds: PmTilesLatLngBounds? = null,
    val coversTargetBounds: Boolean = false
)

data class PmTilesOfflineBasemapReadiness(
    val status: PmTilesOfflineBasemapStatus,
    val ready: Boolean,
    val title: String,
    val caption: String,
    val stepValue: String
)

object PmTilesOfflineBasemapManifestReader {
    fun read(
        directory: File,
        routePackKey: String,
        targetRegionName: String?,
        targetBounds: PmTilesLatLngBounds? = null
    ): PmTilesOfflineBasemapManifest {
        val fileName = "$routePackKey.pmtiles"
        val file = directory.resolve(fileName)
        val inspection = if (file.isFile) {
            PmTilesArchiveHeaderParser.inspect(file)
        } else {
            PmTilesArchiveInspection(header = null, error = null)
        }
        val coversTarget = targetBounds != null && inspection.header?.bounds?.intersects(targetBounds) == true
        return PmTilesOfflineBasemapManifest(
            fileName = fileName,
            fileExists = file.isFile,
            fileSizeBytes = if (file.isFile) file.length() else 0L,
            coveredRegionNames = if (coversTarget) {
                setOfNotNull(targetRegionName?.trim()?.takeIf { it.isNotEmpty() })
            } else {
                emptySet()
            },
            archiveHeader = inspection.header,
            archiveHeaderError = inspection.error,
            targetBounds = targetBounds,
            coversTargetBounds = coversTarget
        )
    }
}

object PmTilesOfflineBasemapStatusEngine {
    fun resolve(
        manifest: PmTilesOfflineBasemapManifest,
        targetRegionName: String?
    ): PmTilesOfflineBasemapReadiness {
        if (!manifest.fileExists) {
            return PmTilesOfflineBasemapReadiness(
                status = PmTilesOfflineBasemapStatus.MISSING_FILE,
                ready = false,
                title = "PMTiles 离线地图包待导入",
                caption = "目标区域还没有本地 PMTiles 地图包，当前继续使用 GPX 路线预览。",
                stepValue = "待导入"
            )
        }
        if (!manifest.fileName.endsWith(".pmtiles", ignoreCase = true)) {
            return PmTilesOfflineBasemapReadiness(
                status = PmTilesOfflineBasemapStatus.INVALID_FORMAT,
                ready = false,
                title = "PMTiles 文件格式不正确",
                caption = "离线地图包必须是 .pmtiles 文件，请重新导入目标区域地图包。",
                stepValue = "需重新导入"
            )
        }
        if (manifest.archiveHeaderError != null || manifest.archiveHeader == null) {
            return PmTilesOfflineBasemapReadiness(
                status = PmTilesOfflineBasemapStatus.INVALID_FORMAT,
                ready = false,
                title = "PMTiles 文件结构不正确",
                caption = "离线地图包缺少有效 PMTiles v3 头部，无法交给 MapLibre 离线渲染。",
                stepValue = "需重新导入"
            )
        }
        if (manifest.archiveHeader.tileType != PmTilesTileType.MVT_VECTOR_TILE) {
            return PmTilesOfflineBasemapReadiness(
                status = PmTilesOfflineBasemapStatus.UNSUPPORTED_TILE_TYPE,
                ready = false,
                title = "PMTiles 瓦片类型暂不支持",
                caption = "当前离线地图包必须是 OSM/Protomaps 向量瓦片，才能在路线页离线渲染。",
                stepValue = "需重新导入"
            )
        }
        if (manifest.fileSizeBytes <= 0L) {
            return PmTilesOfflineBasemapReadiness(
                status = PmTilesOfflineBasemapStatus.EMPTY_FILE,
                ready = false,
                title = "PMTiles 文件不可用",
                caption = "离线地图包为空文件，请重新导入目标区域地图包。",
                stepValue = "需重新导入"
            )
        }
        if (!manifest.covers(targetRegionName)) {
            val region = targetRegionName?.takeIf { it.isNotBlank() } ?: "目标区域"
            return PmTilesOfflineBasemapReadiness(
                status = PmTilesOfflineBasemapStatus.REGION_NOT_COVERED,
                ready = false,
                title = "离线地图包未覆盖目标区域",
                caption = "当前 PMTiles 包未覆盖 $region，实走前需要导入对应区域地图包。",
                stepValue = "待导入$region"
            )
        }

        return PmTilesOfflineBasemapReadiness(
            status = PmTilesOfflineBasemapStatus.READY,
            ready = true,
            title = "PMTiles 离线地图包已导入",
            caption = "本地 PMTiles 地图包覆盖目标区域，可用于离线查看路线周边地图上下文。",
            stepValue = "已导入"
        )
    }

    private fun PmTilesOfflineBasemapManifest.covers(targetRegionName: String?): Boolean {
        if (targetBounds != null) {
            return coversTargetBounds
        }
        val normalizedTarget = targetRegionName?.trim()?.takeIf { it.isNotEmpty() } ?: return true
        return coveredRegionNames.any { region ->
            region.trim().equals(normalizedTarget, ignoreCase = true)
        }
    }
}
