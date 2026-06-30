package com.trailmate.app.feature.route

import com.trailmate.app.core.map.TrailMapProvider
import com.trailmate.app.core.map.TrailMapReadiness
import com.trailmate.app.core.map.TrailMapReadinessStep
import com.trailmate.app.core.map.TrailMapReadinessStepStatus
import com.trailmate.app.core.map.TrailMapSetupHint
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteDetailOfflineBasemapActionTest {
    @Test
    fun importsPmTilesWhenPrimaryActionIsImportBasemap() {
        val readiness = TrailMapReadiness(
            provider = TrailMapProvider.LOCAL_GPX_PREVIEW,
            title = "本地路线预览",
            caption = "PMTiles 离线地图包待导入，当前使用本地 GPX 路线预览。",
            layerChips = listOf("GPX 折线"),
            actionLabel = "导入离线地图包",
            isProductionMapReady = false,
            setupHint = TrailMapSetupHint(
                title = "PMTiles 地图包待导入",
                caption = "导入目标区域 PMTiles 后启用 MapLibre 离线地图上下文。",
                statusLabel = "本地预览"
            ),
            setupSteps = listOf(
                TrailMapReadinessStep(
                    label = "底图",
                    value = "待导入",
                    status = TrailMapReadinessStepStatus.NEEDS_ACTION
                )
            )
        )

        assertTrue(readiness.shouldImportPmTilesBasemap())
    }

    @Test
    fun visibleImportBasemapActionUsesPmTilesImportEvenWhenReadinessActionIsLocalRoute() {
        val readiness = localRouteReadiness(
            actionLabel = "使用本地路线",
            basemapStepValue = "待接入"
        )

        assertTrue(
            shouldOpenPmTilesImport(
                readiness = readiness,
                visibleActionLabel = "导入离线地图包"
            )
        )
    }

    @Test
    fun visibleImportBasemapActionUsesPmTilesImportWithoutProviderGate() {
        val readiness = localRouteReadiness(
            actionLabel = "打开高德离线底图管理",
            basemapStepValue = "待接入"
        ).copy(provider = TrailMapProvider.AMAP_SDK)

        assertTrue(
            shouldOpenPmTilesImport(
                readiness = readiness,
                visibleActionLabel = "导入离线地图包"
            )
        )
    }

    @Test
    fun visiblePrepareOfflineBasemapActionUsesPmTilesImport() {
        val readiness = localRouteReadiness(
            actionLabel = "使用本地路线",
            basemapStepValue = "待接入"
        )

        assertTrue(
            shouldOpenPmTilesImport(
                readiness = readiness,
                visibleActionLabel = "准备离线底图"
            )
        )
    }

    @Test
    fun flightModeVerificationDoesNotOpenPmTilesImport() {
        val readiness = localRouteReadiness(
            actionLabel = "使用本地路线",
            basemapStepValue = "待接入"
        )

        assertFalse(
            shouldOpenPmTilesImport(
                readiness = readiness,
                visibleActionLabel = "飞行模式验证底图"
            )
        )
    }

    private fun localRouteReadiness(
        actionLabel: String,
        basemapStepValue: String
    ): TrailMapReadiness =
        TrailMapReadiness(
            provider = TrailMapProvider.LOCAL_GPX_PREVIEW,
            title = "本地路线预览",
            caption = "当前使用本地 GPX 路线预览。",
            layerChips = listOf("GPX 折线"),
            actionLabel = actionLabel,
            isProductionMapReady = false,
            setupHint = TrailMapSetupHint(
                title = "当前使用本地路线",
                caption = "路线页继续使用本地 GPX 预览。",
                statusLabel = "本地预览"
            ),
            setupSteps = listOf(
                TrailMapReadinessStep(
                    label = "底图",
                    value = basemapStepValue,
                    status = TrailMapReadinessStepStatus.NEEDS_ACTION
                )
            )
        )
}
