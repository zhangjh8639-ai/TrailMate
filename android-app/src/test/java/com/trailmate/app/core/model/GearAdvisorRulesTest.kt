package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GearAdvisorRulesTest {
    @Test
    fun gearRecommendationsDoNotChangeRouteAssessment() {
        val assessment = RouteAssessmentEngine.assess(
            profile = TrailMateSampleData.baselineProfile,
            route = TrailMateSampleData.importedTargetRoute
        )
        val checklist = RouteGearAdvisorEngine.recommend(
            route = TrailMateSampleData.importedTargetRoute,
            assessment = assessment
        )

        assertEquals(MatchLevel.CAUTION, assessment.matchLevel)
        assertEquals(ConfidenceLevel.LOW, assessment.confidenceLevel)
        assertEquals(15.2, assessment.distanceKm, 0.0)
        assertEquals(GearStatus.MISSING, checklist.first { it.category == "Trekking poles" }.status)
    }

    @Test
    fun longAscentRouteRecommendsDynamicEssentials() {
        val assessment = RouteAssessmentEngine.assess(
            profile = TrailMateSampleData.baselineProfile,
            route = TrailMateSampleData.importedTargetRoute
        )

        val checklist = RouteGearAdvisorEngine.recommend(
            route = TrailMateSampleData.importedTargetRoute,
            assessment = assessment
        )

        assertEquals(GearStatus.CHECK, checklist.first { it.category == "Rain shell" }.status)
        assertEquals(GearStatus.CHECK, checklist.first { it.category == "Headlamp" }.status)
        assertEquals(GearStatus.MISSING, checklist.first { it.category == "Trekking poles" }.status)
        assertEquals(GearStatus.MISSING, checklist.first { it.category == "Warm layer" }.status)
        assertEquals(GearStatus.MISSING, checklist.first { it.category == "Extra water" }.status)
    }

    @Test
    fun easyShortRouteDoesNotForceTrekkingPolesOrWarmLayer() {
        val route = ImportedRoute(
            routeName = "Neighborhood Loop",
            fileName = "neighborhood-loop.gpx",
            distanceKm = 3.2,
            ascentMeters = 80,
            status = RouteImportStatus.PARSED,
            pointCount = 40
        )
        val assessment = RouteAssessmentEngine.assess(
            profile = BaselineProfile(
                exerciseFrequency = ExerciseFrequency.THREE_PLUS_PER_WEEK,
                typicalDuration = TypicalDuration.OVER_60,
                experienceLevel = ExperienceLevel.EXPERIENCED,
                ascentExperience = AscentExperience.OVER_800,
                heightCm = 178,
                weightKg = 70,
                commonPackWeightKg = 4
            ),
            route = route
        )

        val checklist = RouteGearAdvisorEngine.recommend(route = route, assessment = assessment)

        assertEquals(MatchLevel.RECOMMENDED, assessment.matchLevel)
        assertEquals(GearStatus.CHECK, checklist.first { it.category == "Rain shell" }.status)
        assertEquals(GearStatus.OPTIONAL, checklist.first { it.category == "Trekking poles" }.status)
        assertEquals(GearStatus.OPTIONAL, checklist.first { it.category == "Warm layer" }.status)
    }

    @Test
    fun historicalEvidenceLineDoesNotCountAsConcreteGearRisk() {
        val route = ImportedRoute(
            routeName = "Neighborhood Loop",
            fileName = "neighborhood-loop.gpx",
            distanceKm = 3.2,
            ascentMeters = 80,
            status = RouteImportStatus.PARSED,
            pointCount = 40
        )
        val assessment = RouteAssessmentEngine.assess(
            profile = TrailMateSampleData.baselineProfile,
            route = route,
            historicalActivities = TrailMateSampleData.historicalActivities
        )

        val checklist = RouteGearAdvisorEngine.recommend(route = route, assessment = assessment)

        assertEquals(ConfidenceLevel.MEDIUM, assessment.confidenceLevel)
        assertTrue(assessment.risks.any { it.contains("Historical GPX evidence", ignoreCase = true) })
        assertEquals(GearStatus.OPTIONAL, checklist.first { it.category == "Warm layer" }.status)
    }
}
