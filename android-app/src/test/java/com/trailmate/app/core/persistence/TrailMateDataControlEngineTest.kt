package com.trailmate.app.core.persistence

import com.trailmate.app.core.model.RecordedTrackPoint
import com.trailmate.app.core.model.TrailMateSampleData
import com.trailmate.app.core.model.TrackRecordingEngine
import org.junit.Assert.assertEquals
import org.junit.Test

class TrailMateDataControlEngineTest {
    @Test
    fun summarizesSavedProfileRouteAndGearForExportPreview() {
        val snapshot = TrailMateSnapshot(
            profile = TrailMateSampleData.baselineProfile,
            importedRoute = TrailMateSampleData.importedTargetRoute,
            historicalActivities = TrailMateSampleData.historicalActivities
        )

        val summary = TrailMateDataControlEngine.summarize(snapshot)

        assertEquals("资料已保存", summary.profileLine)
        assertEquals("龙井山脊 / 15.2 km / +860 m", summary.routeLine)
        assertEquals("装备匹配缓存来自服务端品牌库", summary.gearMatchLine)
        assertEquals(
            "资料：已保存; 路线：龙井山脊，15.2 km，+860 m; 历史：3 条 GPX; 装备匹配：服务端品牌库候选缓存",
            summary.exportPreview
        )
    }

    @Test
    fun summarizesEmptySnapshotWithoutFabricatingRouteOrProfile() {
        val summary = TrailMateDataControlEngine.summarize(TrailMateSnapshot.empty())

        assertEquals("未保存资料", summary.profileLine)
        assertEquals("尚未导入路线", summary.routeLine)
        assertEquals("装备匹配缓存来自服务端品牌库", summary.gearMatchLine)
        assertEquals(
            "资料：未保存; 路线：无; 历史：0 条 GPX; 装备匹配：服务端品牌库候选缓存",
            summary.exportPreview
        )
    }

    @Test
    fun summarizesLatestRecordedTrackForReviewAndExport() {
        val summary = TrailMateDataControlEngine.summarize(
            TrailMateSnapshot(
                profile = TrailMateSampleData.baselineProfile,
                latestTrackRecording = recordedTrack()
            )
        )

        assertEquals("龙井山脊 / 已记录 1.1 km / 2 个点", summary.trackLine)
        assertEquals(
            "资料：已保存; 路线：无; 历史：0 条 GPX; 装备匹配：服务端品牌库候选缓存; 轨迹：龙井山脊，1.1 km，2 个点",
            summary.exportPreview
        )
    }

    private fun recordedTrack() =
        TrackRecordingEngine.finish(
            state = TrackRecordingEngine.appendLocation(
                state = TrackRecordingEngine.appendLocation(
                    state = TrackRecordingEngine.start(routeName = "龙井山脊", nowEpochMillis = 1_000L),
                    point = RecordedTrackPoint(
                        latitude = 30.0,
                        longitude = 120.0,
                        elevationMeters = 100.0,
                        horizontalAccuracyMeters = 8.0,
                        timestampEpochMillis = 1_000L
                    )
                ),
                point = RecordedTrackPoint(
                    latitude = 30.01,
                    longitude = 120.0,
                    elevationMeters = 120.0,
                    horizontalAccuracyMeters = 8.0,
                    timestampEpochMillis = 421_000L
                )
            ),
            nowEpochMillis = 3_000L
        )
}
