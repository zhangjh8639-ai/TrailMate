package com.trailmate.app.core.map

data class AmapTargetRouteRegion(
    val provinceName: String?,
    val cityName: String?,
    val cityCode: String?,
    val adcode: String?
)

object AmapOfflineBaseMapCoverageEngine {
    fun coversTargetRoute(
        status: AmapOfflineBaseMapStatus?,
        targetRegion: AmapTargetRouteRegion?
    ): Boolean {
        if (status == null || targetRegion == null) return false

        return status.downloadedCities.any { it.coversCity(targetRegion) } ||
            status.downloadedProvinces.any { it.coversProvince(targetRegion) }
    }

    private fun AmapOfflineBaseMapRegion.coversCity(targetRegion: AmapTargetRouteRegion): Boolean =
        level == AmapOfflineBaseMapRegionLevel.CITY &&
            (
                adcode.matchesCode(targetRegion.adcode) ||
                    code.matchesCode(targetRegion.cityCode) ||
                    name.matchesName(targetRegion.cityName)
            )

    private fun AmapOfflineBaseMapRegion.coversProvince(targetRegion: AmapTargetRouteRegion): Boolean =
        level == AmapOfflineBaseMapRegionLevel.PROVINCE &&
            (
                name.matchesName(targetRegion.provinceName) ||
                    adcode.matchesProvincePrefix(targetRegion.adcode) ||
                    code.matchesProvincePrefix(targetRegion.adcode)
            )

    private fun String?.matchesCode(other: String?): Boolean {
        val left = normalizedCode()
        val right = other.normalizedCode()
        return left.isNotEmpty() && left == right
    }

    private fun String?.matchesProvincePrefix(other: String?): Boolean {
        val left = normalizedCode()
        val right = other.normalizedCode()
        return left.length >= PROVINCE_PREFIX_LENGTH &&
            right.length >= PROVINCE_PREFIX_LENGTH &&
            left.take(PROVINCE_PREFIX_LENGTH) == right.take(PROVINCE_PREFIX_LENGTH)
    }

    private fun String?.matchesName(other: String?): Boolean {
        val left = normalizedRegionName()
        val right = other.normalizedRegionName()
        return left.isNotEmpty() && left == right
    }

    private fun String?.normalizedCode(): String =
        orEmpty().trim()

    private fun String?.normalizedRegionName(): String =
        orEmpty()
            .trim()
            .removeSuffix("省")
            .removeSuffix("市")
            .removeSuffix("自治区")
            .removeSuffix("特别行政区")

    private const val PROVINCE_PREFIX_LENGTH = 2
}
