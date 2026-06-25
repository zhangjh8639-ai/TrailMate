package com.trailmate.app.core.model

import com.trailmate.app.core.map.TrailMapReadinessEngine
import com.trailmate.app.core.location.TrailMateLocationSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DepartureReadinessEngineTest {
    @Test
    fun recommendsFixingOfflineGpsAndMissingGearBeforeDeparture() {
        val summary = DepartureReadinessEngine.build(
            mapReadiness = TrailMapReadinessEngine.resolve(
                hasAmapKey = false,
                offlineRoutePackReady = false,
                gpsEnabled = false,
                routePointCount = 128
            ),
            offlineRoutePackReady = false,
            gpsEnabled = false,
            gearRecommendations = listOf(
                GearRecommendation(
                    category = "登山杖",
                    status = GearStatus.MISSING,
                    rationale = "长距离路线建议携带。"
                ),
                GearRecommendation(
                    category = "雨衣",
                    status = GearStatus.CHECK,
                    rationale = "出发前检查。"
                )
            )
        )

        assertEquals("出发前还差 4 项", summary.title)
        assertEquals("建议补齐", summary.statusLabel)
        assertEquals("保存离线路线", summary.primaryActionLabel)
        assertTrue(summary.caption.contains("保存离线路线"))
        assertTrue(summary.caption.contains("导入离线地图包"))
        assertTrue(summary.caption.contains("授权定位"))
        assertTrue(summary.caption.contains("补齐 1 件关键装备"))
        assertEquals(listOf("路线", "离线路线", "离线地图包", "定位", "装备"), summary.steps.map { it.label })
        assertEquals("128 点", summary.steps[0].value)
        assertEquals("待保存", summary.steps[1].value)
        assertEquals("待确认", summary.steps[2].value)
        assertEquals("待授权", summary.steps[3].value)
        assertEquals("缺 1 项", summary.steps[4].value)
    }

    @Test
    fun marksDepartureReadyWhenRouteOfflineGpsAndGearAreReady() {
        val summary = DepartureReadinessEngine.build(
            mapReadiness = TrailMapReadinessEngine.resolve(
                hasAmapKey = true,
                amapSdkAvailable = true,
                amapPrivacyConsentAccepted = true,
                offlineRoutePackReady = true,
                gpsEnabled = true,
                routePointCount = 1858
            ),
            offlineRoutePackReady = true,
            offlineBaseMapRegionCount = 1,
            offlineBaseMapCoversTargetRoute = true,
            offlineBaseMapTilesVerifiedWithoutNetwork = true,
            gpsEnabled = true,
            gearRecommendations = listOf(
                GearRecommendation(
                    category = "雨衣",
                    status = GearStatus.COVERED,
                    rationale = "已匹配。"
                ),
                GearRecommendation(
                    category = "头灯",
                    status = GearStatus.CHECK,
                    rationale = "出发前检查电量。"
                )
            )
        )

        assertEquals("出发检查完成", summary.title)
        assertEquals("可以出发", summary.statusLabel)
        assertEquals("开始徒步并记录轨迹", summary.primaryActionLabel)
        assertEquals("已保存", summary.steps.first { it.label == "离线路线" }.value)
        assertEquals("已覆盖目标区域", summary.steps.first { it.label == "离线地图包" }.value)
        assertEquals("关键装备已覆盖", summary.steps.first { it.label == "装备" }.value)
    }

    @Test
    fun marksDepartureReadyWhenPmTilesBasemapIsReady() {
        val summary = DepartureReadinessEngine.build(
            mapReadiness = TrailMapReadinessEngine.resolve(
                hasAmapKey = false,
                mapLibreRuntimeAvailable = true,
                pmTilesBasemapPackReady = true,
                offlineRoutePackReady = true,
                gpsEnabled = true,
                routePointCount = 1858
            ),
            offlineRoutePackReady = true,
            offlineBaseMapRequirement = OfflineBaseMapRequirement.REQUIRED,
            offlineBaseMapRegionCount = 0,
            gpsEnabled = true,
            gearRecommendations = emptyList()
        )

        assertEquals("出发检查完成", summary.title)
        assertEquals("开始徒步并记录轨迹", summary.primaryActionLabel)
        assertEquals("PMTiles 已导入", summary.steps.first { it.label == "离线地图包" }.value)
    }

    @Test
    fun allowsRecommendedRoutesToStartWhenOfflineBaseMapIsOnlyRecommended() {
        val summary = DepartureReadinessEngine.build(
            mapReadiness = TrailMapReadinessEngine.resolve(
                hasAmapKey = true,
                amapSdkAvailable = true,
                amapPrivacyConsentAccepted = true,
                offlineRoutePackReady = true,
                gpsEnabled = true,
                routePointCount = 1858
            ),
            offlineRoutePackReady = true,
            offlineBaseMapRequirement = OfflineBaseMapRequirement.RECOMMENDED,
            offlineBaseMapRegionCount = 0,
            gpsEnabled = true,
            gearRecommendations = emptyList()
        )

        assertEquals("出发检查完成", summary.title)
        assertEquals("可以出发", summary.statusLabel)
        assertEquals("开始徒步并记录轨迹", summary.primaryActionLabel)
        assertEquals("建议下载", summary.steps.first { it.label == "离线地图包" }.value)
        assertTrue(summary.caption.contains("离线地图包建议下载"))
    }

    @Test
    fun keepsCautionRoutesPendingWhenOfflineBaseMapIsRequired() {
        val summary = DepartureReadinessEngine.build(
            mapReadiness = TrailMapReadinessEngine.resolve(
                hasAmapKey = true,
                amapSdkAvailable = true,
                amapPrivacyConsentAccepted = true,
                offlineRoutePackReady = true,
                gpsEnabled = true,
                routePointCount = 1858
            ),
            offlineRoutePackReady = true,
            offlineBaseMapRequirement = OfflineBaseMapRequirement.REQUIRED,
            offlineBaseMapRegionCount = 0,
            gpsEnabled = true,
            gearRecommendations = emptyList()
        )

        assertEquals("出发前还差 1 项", summary.title)
        assertEquals("导入离线地图包", summary.primaryActionLabel)
        assertEquals("未下载", summary.steps.first { it.label == "离线地图包" }.value)
    }

    @Test
    fun explainsWhyRequiredOfflineBaseMapIsDifferentFromSavedRoutePack() {
        val summary = DepartureReadinessEngine.build(
            mapReadiness = TrailMapReadinessEngine.resolve(
                hasAmapKey = true,
                amapSdkAvailable = true,
                amapPrivacyConsentAccepted = true,
                offlineRoutePackReady = true,
                gpsEnabled = true,
                routePointCount = 1858
            ),
            offlineRoutePackReady = true,
            offlineBaseMapRequirement = OfflineBaseMapRequirement.REQUIRED,
            offlineBaseMapRegionCount = 0,
            targetOfflineBaseMapRegionLabel = "杭州市",
            gpsEnabled = true,
            gearRecommendations = emptyList()
        )

        assertEquals("导入离线地图包", summary.primaryActionLabel)
        assertTrue(summary.caption.contains("GPX 路线只保存折线和检查点"))
        assertTrue(summary.caption.contains("弱网"))
        assertTrue(summary.caption.contains("道路"))
        assertTrue(summary.caption.contains("地名"))
        assertTrue(summary.caption.contains("水系"))
        assertTrue(summary.caption.contains("岔路"))
        assertTrue(summary.caption.contains("撤退参照"))
    }

    @Test
    fun namesTargetRegionWhenOfflineBaseMapIsMissing() {
        val summary = DepartureReadinessEngine.build(
            mapReadiness = TrailMapReadinessEngine.resolve(
                hasAmapKey = true,
                amapSdkAvailable = true,
                amapPrivacyConsentAccepted = true,
                offlineRoutePackReady = true,
                gpsEnabled = true,
                routePointCount = 1858
            ),
            offlineRoutePackReady = true,
            offlineBaseMapRequirement = OfflineBaseMapRequirement.REQUIRED,
            offlineBaseMapRegionCount = 0,
            targetOfflineBaseMapRegionLabel = "杭州市",
            gpsEnabled = true,
            gearRecommendations = emptyList()
        )

        assertEquals("导入离线地图包", summary.primaryActionLabel)
        assertTrue(summary.caption.contains("导入离线地图包"))
        assertEquals("杭州市未下载", summary.steps.first { it.label == "离线地图包" }.value)
    }

    @Test
    fun keepsDeparturePendingUntilOfflineBaseMapTilesAreVerifiedWithoutNetwork() {
        val summary = DepartureReadinessEngine.build(
            mapReadiness = TrailMapReadinessEngine.resolve(
                hasAmapKey = true,
                amapSdkAvailable = true,
                amapPrivacyConsentAccepted = true,
                offlineRoutePackReady = true,
                gpsEnabled = true,
                routePointCount = 1858
            ),
            offlineRoutePackReady = true,
            offlineBaseMapRegionCount = 1,
            offlineBaseMapCoversTargetRoute = true,
            offlineBaseMapTilesVerifiedWithoutNetwork = false,
            gpsEnabled = true,
            gearRecommendations = emptyList()
        )

        assertEquals("出发前还差 1 项", summary.title)
        assertEquals("建议补齐", summary.statusLabel)
        assertEquals("飞行模式验证底图", summary.primaryActionLabel)
        assertEquals("已覆盖目标区域，待断网验证", summary.steps.first { it.label == "离线地图包" }.value)
    }

    @Test
    fun keepsDeparturePendingUntilTargetOfflineBaseMapIsVerified() {
        val summary = DepartureReadinessEngine.build(
            mapReadiness = TrailMapReadinessEngine.resolve(
                hasAmapKey = true,
                amapSdkAvailable = true,
                amapPrivacyConsentAccepted = true,
                offlineRoutePackReady = true,
                gpsEnabled = true,
                routePointCount = 1858
            ),
            offlineRoutePackReady = true,
            offlineBaseMapRegionCount = 0,
            gpsEnabled = true,
            gearRecommendations = emptyList()
        )

        assertEquals("出发前还差 1 项", summary.title)
        assertEquals("建议补齐", summary.statusLabel)
        assertEquals("导入离线地图包", summary.primaryActionLabel)
        assertTrue(summary.caption.contains("导入离线地图包"))
        assertEquals(listOf("路线", "离线路线", "离线地图包", "定位", "装备"), summary.steps.map { it.label })
        assertEquals("未下载", summary.steps[2].value)
    }

    @Test
    fun keepsDeparturePendingWhenSystemLocationProviderIsDisabled() {
        val summary = DepartureReadinessEngine.build(
            mapReadiness = TrailMapReadinessEngine.resolve(
                hasAmapKey = true,
                amapSdkAvailable = true,
                amapPrivacyConsentAccepted = true,
                offlineRoutePackReady = true,
                gpsEnabled = true,
                routePointCount = 1858
            ),
            offlineRoutePackReady = true,
            offlineBaseMapRegionCount = 1,
            offlineBaseMapCoversTargetRoute = true,
            offlineBaseMapTilesVerifiedWithoutNetwork = true,
            gpsEnabled = true,
            locationSnapshot = TrailMateLocationSnapshot.providerDisabled(),
            gearRecommendations = emptyList()
        )

        assertEquals("出发前还差 1 项", summary.title)
        assertEquals("建议补齐", summary.statusLabel)
        assertEquals("打开系统定位", summary.primaryActionLabel)
        assertEquals("系统未开启", summary.steps.first { it.label == "定位" }.value)
    }

    @Test
    fun keepsDeparturePendingWhenDownloadedOfflineBaseMapHasNotBeenMatchedToTargetRoute() {
        val summary = DepartureReadinessEngine.build(
            mapReadiness = TrailMapReadinessEngine.resolve(
                hasAmapKey = true,
                amapSdkAvailable = true,
                amapPrivacyConsentAccepted = true,
                offlineRoutePackReady = true,
                gpsEnabled = true,
                routePointCount = 1858
            ),
            offlineRoutePackReady = true,
            offlineBaseMapRegionCount = 2,
            gpsEnabled = true,
            gearRecommendations = emptyList()
        )

        assertEquals("出发前还差 1 项", summary.title)
        assertEquals("建议补齐", summary.statusLabel)
        assertEquals("导入离线地图包", summary.primaryActionLabel)
        assertEquals("已下载 2 区域，待匹配目标路线", summary.steps[2].value)
    }

    @Test
    fun prioritizesRouteReimportWhenGeometryIsMissing() {
        val summary = DepartureReadinessEngine.build(
            mapReadiness = TrailMapReadinessEngine.resolve(
                hasAmapKey = false,
                offlineRoutePackReady = true,
                gpsEnabled = true,
                routePointCount = 1
            ),
            offlineRoutePackReady = true,
            gpsEnabled = true,
            gearRecommendations = emptyList()
        )

        assertEquals("路线文件需处理", summary.title)
        assertEquals("暂不建议出发", summary.statusLabel)
        assertEquals("重新导入 GPX", summary.primaryActionLabel)
        assertTrue(summary.caption.contains("重新导入"))
    }
}
