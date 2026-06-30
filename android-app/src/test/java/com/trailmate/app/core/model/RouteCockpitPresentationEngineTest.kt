package com.trailmate.app.core.model

import com.trailmate.app.core.location.TrailMateLocationSnapshot
import com.trailmate.app.core.map.TrailMapReadinessEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteCockpitPresentationEngineTest {
    @Test
    fun primaryActionPrioritizesOfflineRoutePackBeforeLocationAndStart() {
        val presentation = RouteCockpitPresentationEngine.build(
            route = sampleRoute,
            plan = samplePlan,
            session = HikeSessionState(status = HikeSessionStatus.READY, reachedCheckpointIndex = 0),
            liveGuidance = sampleGuidance,
            mapReadiness = mapReadiness(gpsEnabled = false, offlineRoutePackReady = false),
            departureReadiness = departureReadiness(gpsEnabled = false, offlineRoutePackReady = false),
            locationSnapshot = TrailMateLocationSnapshot.permissionRequired(),
            locationGuidanceStatus = LocationBackedHikeStatus.WAITING,
            trackRecording = TrackRecordingState(),
            wasRecentlyOffRoute = false
        )

        assertEquals(RouteCockpitPrimaryActionKind.SAVE_OFFLINE_ROUTE_PACK, presentation.primaryAction.kind)
        assertEquals("保存离线路线", presentation.primaryAction.label)
        assertEquals("定位", presentation.readinessItems[0].label)
        assertEquals(RouteCockpitReadinessTone.BLOCKED, presentation.readinessItems[0].tone)
    }

    @Test
    fun primaryActionRequestsLocationWhenOfflinePreparationIsReady() {
        val presentation = RouteCockpitPresentationEngine.build(
            route = sampleRoute,
            plan = samplePlan,
            session = HikeSessionState(status = HikeSessionStatus.READY, reachedCheckpointIndex = 0),
            liveGuidance = sampleGuidance,
            mapReadiness = mapReadiness(gpsEnabled = false, offlineRoutePackReady = true),
            departureReadiness = departureReadiness(gpsEnabled = false, offlineRoutePackReady = true),
            locationSnapshot = TrailMateLocationSnapshot.permissionRequired(),
            locationGuidanceStatus = LocationBackedHikeStatus.WAITING,
            trackRecording = TrackRecordingState(),
            wasRecentlyOffRoute = false
        )

        assertEquals(RouteCockpitPrimaryActionKind.REQUEST_LOCATION, presentation.primaryAction.kind)
        assertEquals("授权定位", presentation.primaryAction.label)
    }

    @Test
    fun showsRecordingActionAndReadinessStripWhenTracking() {
        val presentation = RouteCockpitPresentationEngine.build(
            route = sampleRoute,
            plan = samplePlan,
            session = HikeSessionState(status = HikeSessionStatus.ACTIVE, reachedCheckpointIndex = 1),
            liveGuidance = sampleGuidance,
            mapReadiness = mapReadiness(gpsEnabled = true, offlineRoutePackReady = true),
            departureReadiness = departureReadiness(gpsEnabled = true, offlineRoutePackReady = true),
            locationSnapshot = locatedSnapshot,
            locationGuidanceStatus = LocationBackedHikeStatus.ON_ROUTE,
            trackRecording = TrackRecordingState(
                status = TrackRecordingStatus.RECORDING,
                routeName = "龙井山脊",
                points = listOf(sampleTrackPoint)
            ),
            wasRecentlyOffRoute = false
        )

        assertEquals(RouteCockpitPrimaryActionKind.PAUSE_RECORDING, presentation.primaryAction.kind)
        assertEquals("暂停", presentation.primaryAction.label)
        assertEquals("CP1 · 2.3km", presentation.currentCheckpointLabel)
        assertEquals("下一站 终点 · 5.1km", presentation.nextCheckpointLabel)
        assertEquals("1 点", presentation.readinessItems.first { it.label == "记录" }.value)
        assertEquals(
            RouteCockpitReadinessActionKind.PAUSE_RECORDING,
            presentation.readinessItems.first { it.label == "记录" }.actionKind
        )
        assertTrue(presentation.progressLabel.contains("50%"))
    }

    @Test
    fun readinessItemsExposeFieldActions() {
        val presentation = RouteCockpitPresentationEngine.build(
            route = sampleRoute,
            plan = samplePlan,
            session = HikeSessionState(status = HikeSessionStatus.ACTIVE, reachedCheckpointIndex = 1),
            liveGuidance = sampleGuidance,
            mapReadiness = mapReadiness(gpsEnabled = false, offlineRoutePackReady = false),
            departureReadiness = departureReadiness(
                gpsEnabled = false,
                offlineRoutePackReady = false,
                gearRecommendations = listOf(
                    GearRecommendation(
                        category = "头灯",
                        status = GearStatus.MISSING,
                        rationale = "夜间下撤需要备用照明。"
                    )
                )
            ),
            locationSnapshot = TrailMateLocationSnapshot.disabled(),
            locationGuidanceStatus = LocationBackedHikeStatus.WAITING,
            trackRecording = TrackRecordingState(status = TrackRecordingStatus.IDLE),
            wasRecentlyOffRoute = false
        )

        assertEquals(
            RouteCockpitReadinessActionKind.REQUEST_LOCATION,
            presentation.readinessItems.first { it.label == "定位" }.actionKind
        )
        assertEquals(
            RouteCockpitReadinessActionKind.START_RECORDING,
            presentation.readinessItems.first { it.label == "记录" }.actionKind
        )
        assertEquals(
            RouteCockpitReadinessActionKind.SAVE_OFFLINE_ROUTE_PACK,
            presentation.readinessItems.first { it.label == "离线路线" }.actionKind
        )
        assertEquals("待保存", presentation.readinessItems.first { it.label == "离线路线" }.value)
        assertEquals(
            RouteCockpitReadinessActionKind.SHOW_GEAR,
            presentation.readinessItems.first { it.label == "装备" }.actionKind
        )
    }

    @Test
    fun readinessItemsExposeOfflineBaseMapRepairAction() {
        val presentation = RouteCockpitPresentationEngine.build(
            route = sampleRoute,
            plan = samplePlan,
            session = HikeSessionState(status = HikeSessionStatus.ACTIVE, reachedCheckpointIndex = 1),
            liveGuidance = sampleGuidance,
            mapReadiness = mapReadiness(gpsEnabled = true, offlineRoutePackReady = true),
            departureReadiness = departureReadiness(
                gpsEnabled = true,
                offlineRoutePackReady = true,
                offlineBaseMapRegionCount = 0
            ),
            locationSnapshot = locatedSnapshot,
            locationGuidanceStatus = LocationBackedHikeStatus.ON_ROUTE,
            trackRecording = TrackRecordingState(status = TrackRecordingStatus.IDLE),
            wasRecentlyOffRoute = false,
            nowEpochMillis = NOW_EPOCH_MILLIS
        )

        val baseMapItem = presentation.readinessItems.first { it.label == "离线地图包" }
        assertEquals("未下载", baseMapItem.value)
        assertEquals(RouteCockpitReadinessTone.ATTENTION, baseMapItem.tone)
        assertEquals(RouteCockpitReadinessActionKind.OPEN_OFFLINE_BASE_MAP, baseMapItem.actionKind)
    }

    @Test
    fun primaryActionKeepsNavigationWhenOfflineBaseMapIsMissing() {
        val presentation = RouteCockpitPresentationEngine.build(
            route = sampleRoute,
            plan = samplePlan,
            session = HikeSessionState(status = HikeSessionStatus.READY, reachedCheckpointIndex = 0),
            liveGuidance = sampleGuidance,
            mapReadiness = mapReadiness(gpsEnabled = true, offlineRoutePackReady = true),
            departureReadiness = departureReadiness(
                gpsEnabled = true,
                offlineRoutePackReady = true,
                offlineBaseMapRegionCount = 0
            ),
            locationSnapshot = locatedSnapshot,
            locationGuidanceStatus = LocationBackedHikeStatus.ON_ROUTE,
            trackRecording = TrackRecordingState(status = TrackRecordingStatus.IDLE),
            wasRecentlyOffRoute = false,
            nowEpochMillis = NOW_EPOCH_MILLIS
        )

        assertEquals("开始徒步并记录轨迹", presentation.primaryAction.label)
        assertEquals(RouteCockpitPrimaryActionKind.START_HIKE, presentation.primaryAction.kind)
        val baseMapItem = presentation.readinessItems.first { it.label == "离线地图包" }
        assertEquals(RouteCockpitReadinessActionKind.OPEN_OFFLINE_BASE_MAP, baseMapItem.actionKind)
    }

    @Test
    fun primaryActionKeepsNavigationWhenTargetOfflineBaseMapRegionIsMissing() {
        val presentation = RouteCockpitPresentationEngine.build(
            route = sampleRoute,
            plan = samplePlan,
            session = HikeSessionState(status = HikeSessionStatus.READY, reachedCheckpointIndex = 0),
            liveGuidance = sampleGuidance,
            mapReadiness = mapReadiness(gpsEnabled = true, offlineRoutePackReady = true),
            departureReadiness = departureReadiness(
                gpsEnabled = true,
                offlineRoutePackReady = true,
                offlineBaseMapRegionCount = 0,
                targetOfflineBaseMapRegionLabel = "杭州市"
            ),
            locationSnapshot = locatedSnapshot,
            locationGuidanceStatus = LocationBackedHikeStatus.ON_ROUTE,
            trackRecording = TrackRecordingState(status = TrackRecordingStatus.IDLE),
            wasRecentlyOffRoute = false,
            nowEpochMillis = NOW_EPOCH_MILLIS
        )

        assertEquals("开始徒步并记录轨迹", presentation.primaryAction.label)
        assertEquals(RouteCockpitPrimaryActionKind.START_HIKE, presentation.primaryAction.kind)
        val baseMapItem = presentation.readinessItems.first { it.label == "离线地图包" }
        assertEquals("杭州市未下载", baseMapItem.value)
        assertEquals(RouteCockpitReadinessActionKind.OPEN_OFFLINE_BASE_MAP, baseMapItem.actionKind)
    }

    @Test
    fun primaryActionKeepsNavigationWhenOfflineBaseMapTileVerificationIsMissing() {
        val presentation = RouteCockpitPresentationEngine.build(
            route = sampleRoute,
            plan = samplePlan,
            session = HikeSessionState(status = HikeSessionStatus.READY, reachedCheckpointIndex = 0),
            liveGuidance = sampleGuidance,
            mapReadiness = mapReadiness(gpsEnabled = true, offlineRoutePackReady = true),
            departureReadiness = departureReadiness(
                gpsEnabled = true,
                offlineRoutePackReady = true,
                offlineBaseMapRegionCount = 1,
                offlineBaseMapCoversTargetRoute = true,
                offlineBaseMapTilesVerifiedWithoutNetwork = false
            ),
            locationSnapshot = locatedSnapshot,
            locationGuidanceStatus = LocationBackedHikeStatus.ON_ROUTE,
            trackRecording = TrackRecordingState(status = TrackRecordingStatus.IDLE),
            wasRecentlyOffRoute = false,
            nowEpochMillis = NOW_EPOCH_MILLIS
        )

        assertEquals("开始徒步并记录轨迹", presentation.primaryAction.label)
        assertEquals(RouteCockpitPrimaryActionKind.START_HIKE, presentation.primaryAction.kind)
        val baseMapItem = presentation.readinessItems.first { it.label == "离线地图包" }
        assertEquals(RouteCockpitReadinessActionKind.OPEN_OFFLINE_BASE_MAP, baseMapItem.actionKind)
    }

    @Test
    fun primaryActionRequiresCriticalGearBeforeStartingHike() {
        val presentation = RouteCockpitPresentationEngine.build(
            route = sampleRoute,
            plan = samplePlan,
            session = HikeSessionState(status = HikeSessionStatus.READY, reachedCheckpointIndex = 0),
            liveGuidance = sampleGuidance,
            mapReadiness = mapReadiness(gpsEnabled = true, offlineRoutePackReady = true),
            departureReadiness = departureReadiness(
                gpsEnabled = true,
                offlineRoutePackReady = true,
                gearRecommendations = listOf(
                    GearRecommendation(
                        category = "头灯",
                        status = GearStatus.MISSING,
                        rationale = "夜间下撤需要备用照明。"
                    )
                )
            ),
            locationSnapshot = locatedSnapshot,
            locationGuidanceStatus = LocationBackedHikeStatus.ON_ROUTE,
            trackRecording = TrackRecordingState(status = TrackRecordingStatus.IDLE),
            wasRecentlyOffRoute = false,
            nowEpochMillis = NOW_EPOCH_MILLIS
        )

        assertEquals("补齐 1 件关键装备", presentation.primaryAction.label)
        assertEquals(RouteCockpitPrimaryActionKind.SHOW_GEAR, presentation.primaryAction.kind)
        assertEquals("缺 1 项", presentation.readinessItems.first { it.label == "装备" }.value)
    }

    @Test
    fun optionalOfflineBaseMapGapDoesNotBlockStartingHike() {
        val presentation = RouteCockpitPresentationEngine.build(
            route = sampleRoute,
            plan = samplePlan,
            session = HikeSessionState(status = HikeSessionStatus.READY, reachedCheckpointIndex = 0),
            liveGuidance = sampleGuidance,
            mapReadiness = mapReadiness(gpsEnabled = true, offlineRoutePackReady = true),
            departureReadiness = departureReadiness(
                gpsEnabled = true,
                offlineRoutePackReady = true,
                offlineBaseMapRequirement = OfflineBaseMapRequirement.RECOMMENDED,
                offlineBaseMapRegionCount = 0
            ),
            locationSnapshot = locatedSnapshot,
            locationGuidanceStatus = LocationBackedHikeStatus.ON_ROUTE,
            trackRecording = TrackRecordingState(status = TrackRecordingStatus.IDLE),
            wasRecentlyOffRoute = false,
            nowEpochMillis = NOW_EPOCH_MILLIS
        )

        assertEquals("开始徒步并记录轨迹", presentation.primaryAction.label)
        assertEquals(RouteCockpitPrimaryActionKind.START_HIKE, presentation.primaryAction.kind)
        assertEquals("建议下载", presentation.readinessItems.first { it.label == "离线地图包" }.value)
    }

    @Test
    fun activeRecordingKeepsPausePrimaryEvenWhenDepartureRepairsRemain() {
        val presentation = RouteCockpitPresentationEngine.build(
            route = sampleRoute,
            plan = samplePlan,
            session = HikeSessionState(status = HikeSessionStatus.ACTIVE, reachedCheckpointIndex = 1),
            liveGuidance = sampleGuidance,
            mapReadiness = mapReadiness(gpsEnabled = true, offlineRoutePackReady = true),
            departureReadiness = departureReadiness(
                gpsEnabled = true,
                offlineRoutePackReady = true,
                offlineBaseMapRegionCount = 0
            ),
            locationSnapshot = locatedSnapshot,
            locationGuidanceStatus = LocationBackedHikeStatus.ON_ROUTE,
            trackRecording = TrackRecordingState(status = TrackRecordingStatus.RECORDING),
            wasRecentlyOffRoute = false,
            nowEpochMillis = NOW_EPOCH_MILLIS
        )

        assertEquals("暂停", presentation.primaryAction.label)
        assertEquals(RouteCockpitPrimaryActionKind.PAUSE_RECORDING, presentation.primaryAction.kind)
    }

    @Test
    fun restoredRecordingKeepsPausePrimaryEvenWhenSessionIsStillReady() {
        val presentation = RouteCockpitPresentationEngine.build(
            route = sampleRoute,
            plan = samplePlan,
            session = HikeSessionState(status = HikeSessionStatus.READY, reachedCheckpointIndex = 0),
            liveGuidance = sampleGuidance,
            mapReadiness = mapReadiness(gpsEnabled = true, offlineRoutePackReady = true),
            departureReadiness = departureReadiness(
                gpsEnabled = true,
                offlineRoutePackReady = true,
                offlineBaseMapRegionCount = 0
            ),
            locationSnapshot = locatedSnapshot,
            locationGuidanceStatus = LocationBackedHikeStatus.ON_ROUTE,
            trackRecording = TrackRecordingState(status = TrackRecordingStatus.RECORDING),
            wasRecentlyOffRoute = false,
            nowEpochMillis = NOW_EPOCH_MILLIS
        )

        assertEquals("暂停", presentation.primaryAction.label)
        assertEquals(RouteCockpitPrimaryActionKind.PAUSE_RECORDING, presentation.primaryAction.kind)
    }

    @Test
    fun restoredPausedRecordingKeepsResumePrimaryEvenWhenSessionIsStillReady() {
        val presentation = RouteCockpitPresentationEngine.build(
            route = sampleRoute,
            plan = samplePlan,
            session = HikeSessionState(status = HikeSessionStatus.READY, reachedCheckpointIndex = 0),
            liveGuidance = sampleGuidance,
            mapReadiness = mapReadiness(gpsEnabled = true, offlineRoutePackReady = true),
            departureReadiness = departureReadiness(
                gpsEnabled = true,
                offlineRoutePackReady = true,
                offlineBaseMapRegionCount = 0
            ),
            locationSnapshot = locatedSnapshot,
            locationGuidanceStatus = LocationBackedHikeStatus.ON_ROUTE,
            trackRecording = TrackRecordingState(status = TrackRecordingStatus.PAUSED),
            wasRecentlyOffRoute = false,
            nowEpochMillis = NOW_EPOCH_MILLIS
        )

        assertEquals("继续", presentation.primaryAction.label)
        assertEquals(RouteCockpitPrimaryActionKind.RESUME_RECORDING, presentation.primaryAction.kind)
    }

    @Test
    fun primaryActionWaitsForFirstLocationFixBeforeStartingHike() {
        val presentation = RouteCockpitPresentationEngine.build(
            route = sampleRoute,
            plan = samplePlan,
            session = HikeSessionState(status = HikeSessionStatus.READY, reachedCheckpointIndex = 0),
            liveGuidance = sampleGuidance,
            mapReadiness = mapReadiness(gpsEnabled = true, offlineRoutePackReady = true),
            departureReadiness = departureReadiness(gpsEnabled = true, offlineRoutePackReady = true),
            locationSnapshot = TrailMateLocationSnapshot.searching(),
            locationGuidanceStatus = LocationBackedHikeStatus.WAITING,
            trackRecording = TrackRecordingState(status = TrackRecordingStatus.IDLE),
            wasRecentlyOffRoute = false
        )

        assertEquals(RouteCockpitPrimaryActionKind.REQUEST_LOCATION, presentation.primaryAction.kind)
        assertEquals("等待定位稳定", presentation.primaryAction.label)
    }

    @Test
    fun primaryActionWaitsForAccurateLocationBeforeStartingHike() {
        val presentation = RouteCockpitPresentationEngine.build(
            route = sampleRoute,
            plan = samplePlan,
            session = HikeSessionState(status = HikeSessionStatus.READY, reachedCheckpointIndex = 0),
            liveGuidance = sampleGuidance,
            mapReadiness = mapReadiness(gpsEnabled = true, offlineRoutePackReady = true),
            departureReadiness = departureReadiness(gpsEnabled = true, offlineRoutePackReady = true),
            locationSnapshot = lowAccuracySnapshot,
            locationGuidanceStatus = LocationBackedHikeStatus.LOW_ACCURACY,
            trackRecording = TrackRecordingState(status = TrackRecordingStatus.IDLE),
            wasRecentlyOffRoute = false
        )

        assertEquals(RouteCockpitPrimaryActionKind.REQUEST_LOCATION, presentation.primaryAction.kind)
        assertEquals("等待定位稳定", presentation.primaryAction.label)
    }

    @Test
    fun primaryActionWaitsForFreshLocationBeforeStartingHike() {
        val presentation = RouteCockpitPresentationEngine.build(
            route = sampleRoute,
            plan = samplePlan,
            session = HikeSessionState(status = HikeSessionStatus.READY, reachedCheckpointIndex = 0),
            liveGuidance = sampleGuidance,
            mapReadiness = mapReadiness(gpsEnabled = true, offlineRoutePackReady = true),
            departureReadiness = departureReadiness(gpsEnabled = true, offlineRoutePackReady = true),
            locationSnapshot = locatedSnapshot.copy(timestampEpochMillis = NOW_EPOCH_MILLIS - 120_000L),
            locationGuidanceStatus = LocationBackedHikeStatus.ON_ROUTE,
            trackRecording = TrackRecordingState(status = TrackRecordingStatus.IDLE),
            wasRecentlyOffRoute = false,
            nowEpochMillis = NOW_EPOCH_MILLIS
        )

        assertEquals(RouteCockpitPrimaryActionKind.REQUEST_LOCATION, presentation.primaryAction.kind)
        assertEquals("等待定位稳定", presentation.primaryAction.label)
    }

    @Test
    fun primaryActionOpensSystemLocationSettingsWhenProviderIsDisabled() {
        val presentation = RouteCockpitPresentationEngine.build(
            route = sampleRoute,
            plan = samplePlan,
            session = HikeSessionState(status = HikeSessionStatus.READY, reachedCheckpointIndex = 0),
            liveGuidance = sampleGuidance,
            mapReadiness = mapReadiness(gpsEnabled = true, offlineRoutePackReady = true),
            departureReadiness = departureReadiness(
                gpsEnabled = true,
                offlineRoutePackReady = true,
                locationSnapshot = TrailMateLocationSnapshot.providerDisabled()
            ),
            locationSnapshot = TrailMateLocationSnapshot.providerDisabled(),
            locationGuidanceStatus = LocationBackedHikeStatus.WAITING,
            trackRecording = TrackRecordingState(status = TrackRecordingStatus.IDLE),
            wasRecentlyOffRoute = false
        )

        assertEquals(RouteCockpitPrimaryActionKind.OPEN_LOCATION_SETTINGS, presentation.primaryAction.kind)
        assertEquals("打开系统定位", presentation.primaryAction.label)
        assertEquals("系统未开启", presentation.readinessItems.first { it.label == "定位" }.value)
        assertEquals(
            RouteCockpitReadinessActionKind.OPEN_LOCATION_SETTINGS,
            presentation.readinessItems.first { it.label == "定位" }.actionKind
        )
    }

    @Test
    fun primaryActionBlocksUnsupportedDepartureRepairAction() {
        val presentation = RouteCockpitPresentationEngine.build(
            route = sampleRoute,
            plan = samplePlan,
            session = HikeSessionState(status = HikeSessionStatus.READY, reachedCheckpointIndex = 0),
            liveGuidance = sampleGuidance,
            mapReadiness = mapReadiness(gpsEnabled = true, offlineRoutePackReady = true),
            departureReadiness = DepartureReadinessSummary(
                title = "路线文件需处理",
                statusLabel = "暂不建议出发",
                caption = "当前路线缺少可用轨迹点，请重新导入 GPX 后再开始徒步。",
                primaryActionLabel = "重新导入 GPX",
                steps = emptyList()
            ),
            locationSnapshot = locatedSnapshot,
            locationGuidanceStatus = LocationBackedHikeStatus.WAITING,
            trackRecording = TrackRecordingState(status = TrackRecordingStatus.IDLE),
            wasRecentlyOffRoute = false
        )

        assertEquals(RouteCockpitPrimaryActionKind.BLOCKED, presentation.primaryAction.kind)
        assertEquals("重新导入 GPX", presentation.primaryAction.label)
        assertFalse(presentation.primaryAction.enabled)
    }

    @Test
    fun prioritizesRecoveryAdviceOverRecordingControlWhenOffRoute() {
        val presentation = RouteCockpitPresentationEngine.build(
            route = sampleRoute,
            plan = samplePlan,
            session = HikeSessionState(status = HikeSessionStatus.ACTIVE, reachedCheckpointIndex = 1),
            liveGuidance = sampleGuidance,
            mapReadiness = mapReadiness(gpsEnabled = true, offlineRoutePackReady = true),
            departureReadiness = departureReadiness(gpsEnabled = true, offlineRoutePackReady = true),
            locationSnapshot = locatedSnapshot,
            locationGuidanceStatus = LocationBackedHikeStatus.CHECK_ROUTE,
            trackRecording = TrackRecordingState(status = TrackRecordingStatus.RECORDING),
            wasRecentlyOffRoute = false
        )

        assertEquals(RouteCockpitPrimaryActionKind.VIEW_RECOVERY, presentation.primaryAction.kind)
        assertEquals("查看恢复建议", presentation.primaryAction.label)
        assertEquals("需核对路线", presentation.routeMatchLabel)
    }

    @Test
    fun prioritizesRecoveryAdviceOverRecordingControlAfterRecentRejoin() {
        val presentation = RouteCockpitPresentationEngine.build(
            route = sampleRoute,
            plan = samplePlan,
            session = HikeSessionState(status = HikeSessionStatus.ACTIVE, reachedCheckpointIndex = 1),
            liveGuidance = sampleGuidance,
            mapReadiness = mapReadiness(gpsEnabled = true, offlineRoutePackReady = true),
            departureReadiness = departureReadiness(gpsEnabled = true, offlineRoutePackReady = true),
            locationSnapshot = locatedSnapshot,
            locationGuidanceStatus = LocationBackedHikeStatus.ON_ROUTE,
            trackRecording = TrackRecordingState(status = TrackRecordingStatus.RECORDING),
            wasRecentlyOffRoute = true
        )

        assertEquals(RouteCockpitPrimaryActionKind.VIEW_RECOVERY, presentation.primaryAction.kind)
        assertEquals("查看恢复建议", presentation.primaryAction.label)
        assertEquals("需核对路线", presentation.routeMatchLabel)
    }

    @Test
    fun keepsDepartureGatePrimaryBeforeHikeStartsEvenIfRouteStatusNeedsCheck() {
        val presentation = RouteCockpitPresentationEngine.build(
            route = sampleRoute,
            plan = samplePlan,
            session = HikeSessionState(status = HikeSessionStatus.READY, reachedCheckpointIndex = 0),
            liveGuidance = sampleGuidance,
            mapReadiness = mapReadiness(gpsEnabled = false, offlineRoutePackReady = false),
            departureReadiness = departureReadiness(gpsEnabled = false, offlineRoutePackReady = false),
            locationSnapshot = TrailMateLocationSnapshot.permissionRequired(),
            locationGuidanceStatus = LocationBackedHikeStatus.CHECK_ROUTE,
            trackRecording = TrackRecordingState(status = TrackRecordingStatus.IDLE),
            wasRecentlyOffRoute = false
        )

        assertEquals(RouteCockpitPrimaryActionKind.SAVE_OFFLINE_ROUTE_PACK, presentation.primaryAction.kind)
        assertEquals("保存离线路线", presentation.primaryAction.label)
    }

    @Test
    fun sendsFinishedTrackToReview() {
        val presentation = RouteCockpitPresentationEngine.build(
            route = sampleRoute,
            plan = samplePlan,
            session = HikeSessionState(status = HikeSessionStatus.COMPLETED, reachedCheckpointIndex = 2),
            liveGuidance = sampleGuidance,
            mapReadiness = mapReadiness(gpsEnabled = true, offlineRoutePackReady = true),
            departureReadiness = departureReadiness(gpsEnabled = true, offlineRoutePackReady = true),
            locationSnapshot = locatedSnapshot,
            locationGuidanceStatus = LocationBackedHikeStatus.FINISHED,
            trackRecording = TrackRecordingState(
                status = TrackRecordingStatus.FINISHED,
                routeName = "龙井山脊",
                points = listOf(sampleTrackPoint),
                totalDistanceKm = 3.4
            ),
            wasRecentlyOffRoute = false
        )

        assertEquals(RouteCockpitPrimaryActionKind.REVIEW_TRACK, presentation.primaryAction.kind)
        assertEquals("查看轨迹回顾", presentation.primaryAction.label)
        assertEquals("终点 · 5.1km", presentation.currentCheckpointLabel)
    }

    private fun mapReadiness(
        gpsEnabled: Boolean,
        offlineRoutePackReady: Boolean
    ) = TrailMapReadinessEngine.resolve(
        hasAmapKey = true,
        amapSdkAvailable = true,
        amapPrivacyConsentAccepted = true,
        offlineRoutePackReady = offlineRoutePackReady,
        gpsEnabled = gpsEnabled,
        routePointCount = sampleRoute.pointCount
    )

    private fun departureReadiness(
        gpsEnabled: Boolean,
        offlineRoutePackReady: Boolean,
        offlineBaseMapRequirement: OfflineBaseMapRequirement = OfflineBaseMapRequirement.REQUIRED,
        offlineBaseMapRegionCount: Int? = 1,
        offlineBaseMapCoversTargetRoute: Boolean = offlineBaseMapRegionCount != null && offlineBaseMapRegionCount > 0,
        offlineBaseMapTilesVerifiedWithoutNetwork: Boolean = offlineBaseMapCoversTargetRoute,
        targetOfflineBaseMapRegionLabel: String? = null,
        locationSnapshot: TrailMateLocationSnapshot? = null,
        gearRecommendations: List<GearRecommendation> = emptyList()
    ) = DepartureReadinessEngine.build(
        mapReadiness = mapReadiness(gpsEnabled = gpsEnabled, offlineRoutePackReady = offlineRoutePackReady),
        offlineRoutePackReady = offlineRoutePackReady,
        offlineBaseMapRequirement = offlineBaseMapRequirement,
        offlineBaseMapRegionCount = offlineBaseMapRegionCount,
        offlineBaseMapCoversTargetRoute = offlineBaseMapCoversTargetRoute,
        offlineBaseMapTilesVerifiedWithoutNetwork = offlineBaseMapTilesVerifiedWithoutNetwork,
        targetOfflineBaseMapRegionLabel = targetOfflineBaseMapRegionLabel,
        gpsEnabled = gpsEnabled,
        locationSnapshot = locationSnapshot,
        gearRecommendations = gearRecommendations
    )

    private companion object {
        val sampleRoute = ImportedRoute(
            routeName = "龙井山脊",
            fileName = "longjing.gpx",
            distanceKm = 5.1,
            ascentMeters = 420,
            status = RouteImportStatus.PARSED,
            pointCount = 128,
            routePoints = listOf(
                RoutePoint(latitude = 30.2, longitude = 120.1, elevationMeters = 100.0, distanceAlongRouteKm = 0.0),
                RoutePoint(latitude = 30.3, longitude = 120.2, elevationMeters = 320.0, distanceAlongRouteKm = 5.1)
            )
        )
        val samplePlan = HikePlanSummary(
            checkpoints = listOf(
                HikePlanCheckpoint(
                    type = HikePlanCheckpointType.START,
                    title = "起点",
                    distanceKm = 0.0,
                    timeFromStart = "0:00",
                    note = "准备出发。"
                ),
                HikePlanCheckpoint(
                    type = HikePlanCheckpointType.ENERGY_CHECK,
                    title = "CP1",
                    distanceKm = 2.3,
                    timeFromStart = "1:10",
                    note = "补水。"
                ),
                HikePlanCheckpoint(
                    type = HikePlanCheckpointType.FINISH,
                    title = "终点",
                    distanceKm = 5.1,
                    timeFromStart = "2:40",
                    note = "结束。"
                )
            )
        )
        val sampleGuidance = LiveCheckpointGuidance(
            title = "下一提示：CP2",
            distanceLabel = "5.1 km",
            readinessLabel = "装备可用",
            caption = "保持节奏。"
        )
        val locatedSnapshot = TrailMateLocationSnapshot(
            status = com.trailmate.app.core.location.TrailMateLocationStatus.LOCATED,
            latitude = 30.2,
            longitude = 120.1,
            elevationMeters = 160.0,
            horizontalAccuracyMeters = 12.0,
            timestampEpochMillis = NOW_EPOCH_MILLIS
        )
        val sampleTrackPoint = RecordedTrackPoint(
            latitude = 30.2,
            longitude = 120.1,
            elevationMeters = 160.0,
            horizontalAccuracyMeters = 12.0,
            timestampEpochMillis = 1_700_000_000_000
        )
        val lowAccuracySnapshot = TrailMateLocationSnapshot(
            status = com.trailmate.app.core.location.TrailMateLocationStatus.LOW_ACCURACY,
            latitude = 30.2,
            longitude = 120.1,
            elevationMeters = 160.0,
            horizontalAccuracyMeters = 120.0,
            timestampEpochMillis = NOW_EPOCH_MILLIS
        )
        const val NOW_EPOCH_MILLIS = 1_700_000_060_000L
    }
}
