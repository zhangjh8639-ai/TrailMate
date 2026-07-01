package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.Duration
import java.time.Instant

class SafetyRecordModelsTest {
    @Test
    fun emergencyCardCopyAvoidsRescuePromises() {
        val helperText = SafetyCopy.emergencyHelperText()

        assertEquals("请将以下信息发送给可信联系人或救援人员。", helperText)
        listOf("已自动联系救援", "救援正在赶来", "自动救援已启动", "保证安全").forEach { unsafe ->
            assertFalse(helperText.contains(unsafe))
        }
    }

    @Test
    fun confirmedOffRouteCopyAvoidsDirectReturnPromises() {
        val copy = SafetyCopy.confirmedOffRouteGuidance(
            direction = CompassDirection.Northwest,
            distance = Distance.meters(110.0),
        )

        assertTrue(copy.contains("请结合实际地形返回"))
        assertTrue(copy.contains("不要直线穿越未知区域"))
        listOf("安全路线", "保证安全", "直行返回").forEach { unsafe ->
            assertFalse(copy.contains(unsafe))
        }
    }

    @Test
    fun emergencyCardCarriesLocationStatusWithoutClaimingRescue() {
        val card = EmergencyCard(
            routeName = "九溪十八涧 · 龙井环线",
            coordinate = GeoCoordinate(30.245, 120.116, Elevation.meters(92.0)),
            batteryLevel = BatteryLevel(86),
            gpsAccuracy = GpsAccuracy(8.0),
            nearestExit = RouteExitPoint("exit-road", "最近公路", ExitPointType.RoadAccess, Distance.meters(1100.0)),
            updatedAt = Instant.parse("2026-07-01T02:00:00Z"),
            contactName = "紧急联系人",
        )

        assertEquals("最近公路", card.nearestExit?.title)
        assertEquals("紧急联系人", card.contactName)
        assertEquals("请将以下信息发送给可信联系人或救援人员。", card.helperText)
    }

    @Test
    fun routeRecordDefaultsToPrivateVisibility() {
        val record = RouteRecord.create(
            id = "record-1",
            routeId = RouteId("longjing"),
            title = "九溪十八涧 · 龙井环线",
            completedAt = Instant.parse("2026-07-01T04:00:00Z"),
            actualDistance = Distance.meters(8900.0),
            actualElevationGain = Elevation.meters(448.0),
            duration = Duration.ofHours(3).plusMinutes(38),
            offRouteCount = 2,
            stopCount = 3,
            maxDeviation = Distance.meters(72.0),
            highestElevation = Elevation.meters(388.0),
        )

        assertEquals(PrivacyVisibility.Private, record.visibility)
        assertEquals(Distance.meters(72.0), record.maxDeviation)
    }

    @Test
    fun feedbackCategoriesStayTrailConditionFocused() {
        val categories = FeedbackCategory.entries.toSet()

        assertTrue(categories.contains(FeedbackCategory.Closure))
        assertTrue(categories.contains(FeedbackCategory.Muddy))
        assertTrue(categories.contains(FeedbackCategory.Supply))
        assertTrue(categories.contains(FeedbackCategory.Danger))
        assertTrue(categories.contains(FeedbackCategory.BeginnerSuitability))
    }

    @Test
    fun domainSourceDoesNotIntroduceDeprecatedProductScopes() {
        val sourceRoot = File("app/src/main/java/com/trailmate/app")
        val paths = sourceRoot
            .walkTopDown()
            .filter { it.isFile }
            .map { it.invariantSeparatorsPath.lowercase() }
            .toList()

        listOf("planner", "equipment", "community", "marketplace", "pretrip").forEach { banned ->
            assertFalse(
                "Deprecated product scope leaked into source path: $banned",
                paths.any { it.contains(banned) },
            )
        }
    }
}
