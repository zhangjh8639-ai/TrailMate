package com.trailmate.app.core.model

import com.trailmate.app.core.location.LocationReliabilityDetail
import com.trailmate.app.core.location.LocationReliabilityLevel
import com.trailmate.app.core.location.LocationReliabilityPresentation
import com.trailmate.app.core.map.TrailMapProvider
import com.trailmate.app.core.map.TrailMapReadiness
import com.trailmate.app.core.map.TrailMapSetupHint
import org.junit.Assert.assertEquals
import org.junit.Test

class RouteFieldStatusEngineTest {
    @Test
    fun recordingStatusSummarizesTrustedGpsTrackAndMap() {
        val status = RouteFieldStatusEngine.build(
            mapReadiness = TrailMapReadiness(
                provider = TrailMapProvider.LOCAL_GPX_PREVIEW,
                title = "定位与记录",
                caption = "离线路线已保存，当前位置可用于检查点推进。",
                layerChips = listOf("GPX 折线"),
                actionLabel = "查看路线辅助",
                isProductionMapReady = false,
                setupHint = TrailMapSetupHint(
                    title = "离线与定位已可用",
                    caption = "当前可用本地 GPX 预览和实走轨迹记录。",
                    statusLabel = "实走可用"
                )
            ),
            locationReliability = LocationReliabilityPresentation(
                title = "定位可用于导航",
                statusLabel = "可靠",
                caption = "当前位置已贴近路线。",
                level = LocationReliabilityLevel.GOOD,
                details = listOf(LocationReliabilityDetail(label = "定位精度", value = "约 8 m"))
            ),
            trackRecording = recordedTrack(status = TrackRecordingStatus.RECORDING),
            notificationPermissionGranted = true
        )

        assertEquals("正在记录实走轨迹", status.title)
        assertEquals("记录中", status.statusLabel)
        assertEquals("前台服务已开启，锁屏或切后台后仍会保存可信定位点。", status.caption)
        assertEquals("可靠", status.items.first { item -> item.label == "定位" }.value)
        assertEquals("2 点", status.items.first { item -> item.label == "轨迹" }.value)
        assertEquals("实走可用", status.items.first { item -> item.label == "底图" }.value)
        assertEquals("可锁屏", status.items.first { item -> item.label == "通知" }.value)
        assertEquals("未知", status.items.first { item -> item.label == "电量" }.value)
        assertEquals(listOf("定位", "轨迹", "底图"), status.items.take(3).map { item -> item.label })
    }

    @Test
    fun recordingWithWeakLocationExplainsThatTrackPointsAreWaitingForReliableFix() {
        val status = RouteFieldStatusEngine.build(
            mapReadiness = TrailMapReadiness(
                provider = TrailMapProvider.LOCAL_GPX_PREVIEW,
                title = "定位与记录",
                caption = "离线路线已保存，当前位置可用于检查点推进。",
                layerChips = listOf("GPX 折线"),
                actionLabel = "查看路线辅助",
                isProductionMapReady = false,
                setupHint = TrailMapSetupHint(
                    title = "离线与定位已可用",
                    caption = "当前可用本地 GPX 预览和实走轨迹记录。",
                    statusLabel = "实走可用"
                )
            ),
            locationReliability = LocationReliabilityPresentation(
                title = "定位精度偏低",
                statusLabel = "谨慎使用",
                caption = "尽量到开阔处等待信号稳定，再依赖路线校验。",
                level = LocationReliabilityLevel.CAUTION,
                details = listOf(LocationReliabilityDetail(label = "定位精度", value = "约 118 m"))
            ),
            trackRecording = recordedTrack(status = TrackRecordingStatus.RECORDING),
            notificationPermissionGranted = true
        )

        assertEquals("正在记录实走轨迹", status.title)
        assertEquals("记录中 · 等信号", status.statusLabel)
        assertEquals("正在等待定位稳定；只会把可信定位点写入轨迹。", status.caption)
        assertEquals("谨慎使用", status.items.first { item -> item.label == "定位" }.value)
    }

    @Test
    fun disabledGpsStatusPromptsFieldSetupBeforeHiking() {
        val status = RouteFieldStatusEngine.build(
            mapReadiness = TrailMapReadiness(
                provider = TrailMapProvider.LOCAL_GPX_PREVIEW,
                title = "本地路线预览",
                caption = "在线底图暂不可用，当前使用本地 GPX 路线预览。",
                layerChips = listOf("GPX 折线"),
                actionLabel = "使用本地路线",
                isProductionMapReady = false,
                setupHint = TrailMapSetupHint(
                    title = "当前使用本地路线",
                    caption = "可继续查看 GPX、检查点和记录轨迹；在线底图暂不可用。",
                    statusLabel = "本地预览"
                )
            ),
            locationReliability = LocationReliabilityPresentation(
            title = "授权定位后开始校准",
                statusLabel = "未启用",
                caption = "用于实时位置、路线校验和轨迹记录。",
                level = LocationReliabilityLevel.OFF,
                details = emptyList()
            ),
            trackRecording = TrackRecordingState(),
            notificationPermissionGranted = false
        )

        assertEquals("准备定位记录", status.title)
        assertEquals("待定位", status.statusLabel)
        assertEquals("先授权定位；出发前建议保存离线路线并允许轨迹通知。", status.caption)
        assertEquals("未启用", status.items.first { item -> item.label == "定位" }.value)
        assertEquals("0 点", status.items.first { item -> item.label == "轨迹" }.value)
        assertEquals("待允许", status.items.first { item -> item.label == "通知" }.value)
    }

    @Test
    fun normalBatteryIsShownInFieldStatusWithoutChangingRecordingCaption() {
        val status = buildRecordingStatusWithBattery(RouteBatteryStatus.fromPercent(68))

        assertEquals("68%", status.items.first { item -> item.label == "电量" }.value)
        assertEquals("前台服务已开启，锁屏或切后台后仍会保存可信定位点。", status.caption)
    }

    @Test
    fun lowBatteryShowsConservativeFieldGuidance() {
        val status = buildRecordingStatusWithBattery(RouteBatteryStatus.fromPercent(24))

        assertEquals("偏低 24%", status.items.first { item -> item.label == "电量" }.value)
        assertEquals("电量偏低，建议降低屏幕常亮和后台耗电；优先确认返程、补电或缩短路线。", status.caption)
    }

    @Test
    fun criticalBatteryShowsImmediateSafetyGuidance() {
        val status = buildRecordingStatusWithBattery(RouteBatteryStatus.fromPercent(12))

        assertEquals("危险 12%", status.items.first { item -> item.label == "电量" }.value)
        assertEquals("电量危险，请立即降低屏幕使用，优先撤退或补电，避免继续依赖手机导航。", status.caption)
    }

    @Test
    fun invalidBatteryPercentFallsBackToUnknown() {
        val status = buildRecordingStatusWithBattery(RouteBatteryStatus.fromPercent(140))

        assertEquals("未知", status.items.first { item -> item.label == "电量" }.value)
    }

    @Test
    fun missingBatteryPercentFallsBackToUnknown() {
        val status = buildRecordingStatusWithBattery(RouteBatteryStatus.fromPercent(null))

        assertEquals("未知", status.items.first { item -> item.label == "电量" }.value)
    }

    private fun buildRecordingStatusWithBattery(batteryStatus: RouteBatteryStatus): RouteFieldStatusSummary =
        RouteFieldStatusEngine.build(
            mapReadiness = TrailMapReadiness(
                provider = TrailMapProvider.LOCAL_GPX_PREVIEW,
                title = "定位与记录",
                caption = "离线路线已保存，当前位置可用于检查点推进。",
                layerChips = listOf("GPX 折线"),
                actionLabel = "查看路线辅助",
                isProductionMapReady = false,
                setupHint = TrailMapSetupHint(
                    title = "离线与定位已可用",
                    caption = "当前可用本地 GPX 预览和实走轨迹记录。",
                    statusLabel = "实走可用"
                )
            ),
            locationReliability = LocationReliabilityPresentation(
                title = "定位可用于导航",
                statusLabel = "可靠",
                caption = "当前位置已贴近路线。",
                level = LocationReliabilityLevel.GOOD,
                details = listOf(LocationReliabilityDetail(label = "定位精度", value = "约 8 m"))
            ),
            trackRecording = recordedTrack(status = TrackRecordingStatus.RECORDING),
            notificationPermissionGranted = true,
            batteryStatus = batteryStatus
        )

    private fun recordedTrack(status: TrackRecordingStatus): TrackRecordingState {
        val recording = TrackRecordingEngine.appendLocation(
            state = TrackRecordingEngine.appendLocation(
                state = TrackRecordingEngine.start(routeName = "龙井山脊", nowEpochMillis = 1_000L),
                point = RecordedTrackPoint(
                    latitude = 30.0,
                    longitude = 120.0,
                    elevationMeters = 100.0,
                    horizontalAccuracyMeters = 8.0,
                    timestampEpochMillis = 2_000L
                ),
                nowEpochMillis = 2_000L
            ),
            point = RecordedTrackPoint(
                latitude = 30.01,
                longitude = 120.0,
                elevationMeters = 120.0,
                horizontalAccuracyMeters = 8.0,
                timestampEpochMillis = 422_000L
            ),
            nowEpochMillis = 422_000L
        )

        return when (status) {
            TrackRecordingStatus.RECORDING -> recording
            TrackRecordingStatus.PAUSED -> TrackRecordingEngine.pause(recording, nowEpochMillis = 500_000L)
            TrackRecordingStatus.FINISHED -> TrackRecordingEngine.finish(recording, nowEpochMillis = 500_000L)
            TrackRecordingStatus.IDLE -> TrackRecordingState()
        }
    }
}
