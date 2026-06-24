package com.trailmate.app.core.map

enum class PmTilesOfflineBasemapImportStatus {
    READY_TO_IMPORT,
    INVALID_EXTENSION,
    EMPTY_FILE,
    INVALID_ARCHIVE,
    UNSUPPORTED_TILE_TYPE,
    REGION_NOT_COVERED
}

data class PmTilesOfflineBasemapImportCandidate(
    val displayName: String?,
    val sizeBytes: Long?,
    val archiveInspection: PmTilesArchiveInspection? = null
)

data class PmTilesOfflineBasemapImportDecision(
    val status: PmTilesOfflineBasemapImportStatus,
    val canImport: Boolean,
    val targetFileName: String,
    val title: String,
    val caption: String
)

object PmTilesOfflineBasemapImportPolicy {
    fun resolve(
        candidate: PmTilesOfflineBasemapImportCandidate,
        routePackKey: String,
        targetBounds: PmTilesLatLngBounds? = null
    ): PmTilesOfflineBasemapImportDecision {
        val targetFileName = "$routePackKey.pmtiles"
        val displayName = candidate.displayName.orEmpty()
        if (!displayName.endsWith(".pmtiles", ignoreCase = true)) {
            return PmTilesOfflineBasemapImportDecision(
                status = PmTilesOfflineBasemapImportStatus.INVALID_EXTENSION,
                canImport = false,
                targetFileName = targetFileName,
                title = "请选择 .pmtiles 文件",
                caption = "TrailMate 当前只支持 PMTiles 离线地图包。"
            )
        }
        if (candidate.sizeBytes != null && candidate.sizeBytes <= 0L) {
            return PmTilesOfflineBasemapImportDecision(
                status = PmTilesOfflineBasemapImportStatus.EMPTY_FILE,
                canImport = false,
                targetFileName = targetFileName,
                title = "PMTiles 文件为空",
                caption = "离线地图包没有可读取内容，请重新选择文件。"
            )
        }
        val inspection = candidate.archiveInspection
        if (inspection == null || inspection.header == null || inspection.error != null) {
            return PmTilesOfflineBasemapImportDecision(
                status = PmTilesOfflineBasemapImportStatus.INVALID_ARCHIVE,
                canImport = false,
                targetFileName = targetFileName,
                title = "PMTiles 文件结构不正确",
                caption = "所选文件缺少有效 PMTiles v3 头部，无法作为 MapLibre 离线地图包使用。"
            )
        }
        if (inspection.header.tileType != PmTilesTileType.MVT_VECTOR_TILE) {
            return PmTilesOfflineBasemapImportDecision(
                status = PmTilesOfflineBasemapImportStatus.UNSUPPORTED_TILE_TYPE,
                canImport = false,
                targetFileName = targetFileName,
                title = "PMTiles 瓦片类型暂不支持",
                caption = "TrailMate 当前只支持 OSM/Protomaps 向量瓦片底图。"
            )
        }
        if (targetBounds != null && !inspection.header.bounds.intersects(targetBounds)) {
            return PmTilesOfflineBasemapImportDecision(
                status = PmTilesOfflineBasemapImportStatus.REGION_NOT_COVERED,
                canImport = false,
                targetFileName = targetFileName,
                title = "PMTiles 未覆盖当前路线",
                caption = "所选离线地图包没有覆盖当前 GPX 路线区域，请选择目标区域的 PMTiles 包。"
            )
        }

        return PmTilesOfflineBasemapImportDecision(
            status = PmTilesOfflineBasemapImportStatus.READY_TO_IMPORT,
            canImport = true,
            targetFileName = targetFileName,
            title = "准备导入 PMTiles 离线地图包",
            caption = "文件将保存为当前路线的本地离线地图包。"
        )
    }
}
