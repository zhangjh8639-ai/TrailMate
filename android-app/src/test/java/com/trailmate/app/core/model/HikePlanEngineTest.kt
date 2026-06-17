package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HikePlanEngineTest {
    @Test
    fun sampleRoutePlanCreatesBoundedDeterministicCheckpoints() {
        val plan = HikePlanEngine.build(
            route = TrailMateSampleData.importedTargetRoute,
            assessment = TrailMateSampleData.routeAssessment
        )

        assertEquals(HikePlanCheckpointType.START, plan.checkpoints.first().type)
        assertEquals(HikePlanCheckpointType.FINISH, plan.checkpoints.last().type)
        assertTrue(plan.checkpoints.any { it.type == HikePlanCheckpointType.ENERGY_CHECK })
        assertTrue(plan.checkpoints.any { it.type == HikePlanCheckpointType.RISK_CHECK })
        assertTrue(plan.checkpointCount <= 6)
        assertEquals(
            TrailMateSampleData.routeAssessment.estimatedDurationRange.substringAfter("-"),
            plan.checkpoints.last().timeFromStart
        )
    }

    @Test
    fun recommendedRouteStillGetsLightNavigationPlan() {
        val route = ImportedRoute(
            routeName = "Short Ridge",
            fileName = "short-ridge.gpx",
            distanceKm = 6.0,
            ascentMeters = 220,
            status = RouteImportStatus.PARSED,
            pointCount = 48
        )
        val assessment = RouteAssessmentEngine.assess(
            profile = BaselineProfile(
                exerciseFrequency = ExerciseFrequency.THREE_PLUS_PER_WEEK,
                typicalDuration = TypicalDuration.OVER_60,
                experienceLevel = ExperienceLevel.EXPERIENCED,
                ascentExperience = AscentExperience.OVER_800,
                heightCm = 178,
                weightKg = 70,
                commonPackWeightKg = 6
            ),
            route = route
        )

        val plan = HikePlanEngine.build(route = route, assessment = assessment)

        assertEquals(MatchLevel.RECOMMENDED, assessment.matchLevel)
        assertTrue(plan.checkpoints.any { it.type == HikePlanCheckpointType.REST_CHECK })
        assertEquals(HikePlanCheckpointType.FINISH, plan.checkpoints.last().type)
        assertTrue(plan.checkpoints.last().distanceKm >= route.distanceKm)
    }

    @Test
    fun planNotesAvoidSafetyGuarantees() {
        val plan = HikePlanEngine.build(
            route = TrailMateSampleData.importedTargetRoute,
            assessment = TrailMateSampleData.routeAssessment
        )
        val notes = plan.checkpoints.joinToString(" ") { it.note.lowercase() }

        assertFalse(notes.contains("guarantee"))
        assertFalse(notes.contains("safe to continue"))
        assertFalse(notes.contains("medical"))
    }

    @Test
    fun shortRouteWithoutRisksUsesFallbackDurationAndMinimalCheckpoints() {
        val route = ImportedRoute(
            routeName = "Pocket Loop",
            fileName = "pocket-loop.gpx",
            distanceKm = 1.0,
            ascentMeters = 0,
            status = RouteImportStatus.PARSED,
            pointCount = 20
        )
        val assessment = RouteAssessmentSummary(
            routeName = route.routeName,
            distanceKm = route.distanceKm,
            ascentMeters = route.ascentMeters,
            matchLevel = MatchLevel.RECOMMENDED,
            confidenceLevel = ConfidenceLevel.LOW,
            estimatedDurationRange = "TBD",
            risks = emptyList()
        )

        val plan = HikePlanEngine.build(route = route, assessment = assessment)

        assertEquals(
            listOf(
                HikePlanCheckpointType.START,
                HikePlanCheckpointType.REST_CHECK,
                HikePlanCheckpointType.FINISH
            ),
            plan.checkpoints.map { it.type }
        )
        assertEquals("0:30", plan.checkpoints.last().timeFromStart)
        assertEquals(1.0, plan.checkpoints.last().distanceKm, 0.0)
    }
}
