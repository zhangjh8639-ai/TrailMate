package com.trailmate.app.core.map

import android.content.Context
import com.amap.api.maps.offlinemap.OfflineMapCity
import com.amap.api.maps.offlinemap.OfflineMapManager
import com.amap.api.maps.offlinemap.OfflineMapProvince
import com.amap.api.maps.offlinemap.OfflineMapStatus

enum class AmapOfflineBaseMapRegionLevel {
    CITY,
    PROVINCE
}

data class AmapOfflineBaseMapRegion(
    val name: String,
    val code: String?,
    val adcode: String?,
    val level: AmapOfflineBaseMapRegionLevel
)

data class AmapOfflineBaseMapPendingRegion(
    val name: String,
    val code: String?,
    val adcode: String?,
    val completePercent: Int,
    val stateLabel: String
) {
    val displayLabel: String
        get() = if (completePercent > 0) {
            "$name $completePercent%"
        } else {
            "$name $stateLabel"
        }
}

data class AmapOfflineBaseMapStatus(
    val downloadedCities: List<AmapOfflineBaseMapRegion>,
    val downloadedProvinces: List<AmapOfflineBaseMapRegion>,
    val pendingCities: List<AmapOfflineBaseMapPendingRegion> = emptyList()
) {
    val downloadedCityCount: Int
        get() = downloadedCities.size

    val downloadedProvinceCount: Int
        get() = downloadedProvinces.size

    val downloadedRegionCount: Int
        get() = downloadedCityCount + downloadedProvinceCount

    val pendingRegionCount: Int
        get() = pendingCities.size

    val pendingRegionLabels: List<String>
        get() = pendingCities.map { it.displayLabel }
}

object AmapOfflineBaseMapStatusReader {
    fun readDownloadedStatus(context: Context): AmapOfflineBaseMapStatus? =
        runCatching {
            AmapSdkInitializer.initialize(context)
            val manager = OfflineMapManager(
                context.applicationContext,
                object : OfflineMapManager.OfflineMapDownloadListener {
                    override fun onDownload(status: Int, completeCode: Int, cityName: String?) = Unit
                    override fun onCheckUpdate(hasNew: Boolean, name: String?) = Unit
                    override fun onRemove(success: Boolean, name: String?, describe: String?) = Unit
                }
            )
            try {
                val downloadedCities = manager.getDownloadOfflineMapCityList()
                    .orEmpty()
                    .filter(::hasDownloadedState)
                val downloadedProvinces = manager.getDownloadOfflineMapProvinceList()
                    .orEmpty()
                    .filter { province ->
                        province.state == OfflineMapStatus.SUCCESS ||
                            province.completeCode >= COMPLETE_PERCENT
                    }
                val pendingCities = manager.getDownloadingCityList()
                    .orEmpty()
                    .filterNot(::hasDownloadedState)
                AmapOfflineBaseMapStatus(
                    downloadedCities = downloadedCities.map(::toDownloadedCityRegion),
                    downloadedProvinces = downloadedProvinces.map(::toDownloadedProvinceRegion),
                    pendingCities = pendingCities.map(::toPendingCityRegion)
                )
            } finally {
                manager.destroy()
            }
        }.getOrNull()

    private fun hasDownloadedState(city: OfflineMapCity): Boolean =
        city.state == OfflineMapStatus.SUCCESS || city.getcompleteCode() >= COMPLETE_PERCENT

    private fun toDownloadedCityRegion(city: OfflineMapCity): AmapOfflineBaseMapRegion =
        AmapOfflineBaseMapRegion(
            name = city.city.orEmpty(),
            code = city.code,
            adcode = city.adcode,
            level = AmapOfflineBaseMapRegionLevel.CITY
        )

    private fun toDownloadedProvinceRegion(province: OfflineMapProvince): AmapOfflineBaseMapRegion =
        AmapOfflineBaseMapRegion(
            name = province.provinceName.orEmpty(),
            code = province.provinceCode,
            adcode = province.provinceCode,
            level = AmapOfflineBaseMapRegionLevel.PROVINCE
        )

    private fun toPendingCityRegion(city: OfflineMapCity): AmapOfflineBaseMapPendingRegion =
        AmapOfflineBaseMapPendingRegion(
            name = city.city.orEmpty(),
            code = city.code,
            adcode = city.adcode,
            completePercent = city.getcompleteCode(),
            stateLabel = city.state.toOfflineStateLabel()
        )

    private fun Int.toOfflineStateLabel(): String =
        when (this) {
            OfflineMapStatus.LOADING -> "下载中"
            OfflineMapStatus.UNZIP -> "解压中"
            OfflineMapStatus.WAITING -> "等待中"
            OfflineMapStatus.PAUSE -> "已暂停"
            OfflineMapStatus.STOP -> "已停止"
            OfflineMapStatus.CHECKUPDATES -> "检查更新"
            OfflineMapStatus.NEW_VERSION -> "有更新"
            OfflineMapStatus.EXCEPTION_NETWORK_LOADING -> "网络异常"
            OfflineMapStatus.EXCEPTION_AMAP -> "服务异常"
            OfflineMapStatus.EXCEPTION_SDCARD -> "存储异常"
            OfflineMapStatus.START_DOWNLOAD_FAILED -> "启动失败"
            else -> "待完成"
        }

    private const val COMPLETE_PERCENT = 100
}
