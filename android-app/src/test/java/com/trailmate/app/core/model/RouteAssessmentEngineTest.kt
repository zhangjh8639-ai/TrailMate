package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteAssessmentEngineTest {
    @Test
    fun assessmentUsesImportedRouteAndQuestionnaireProfile() {
        val assessment = RouteAssessmentEngine.assess(
            profile = TrailMateSampleData.baselineProfile,
            route = TrailMateSampleData.importedTargetRoute
        )

        assertEquals("Longjing Ridge", assessment.routeName)
        assertEquals(15.2, assessment.distanceKm, 0.0)
        assertEquals(860, assessment.ascentMeters)
        assertEquals(ConfidenceLevel.LOW, assessment.confidenceLevel)
        assertEquals(MatchLevel.CAUTION, assessment.matchLevel)
        assertTrue(assessment.risks.any { it.contains("ascent", ignoreCase = true) })
    }

    @Test
    fun easyRouteCanBeRecommendedForExperiencedProfile() {
        val profile = BaselineProfile(
            exerciseFrequency = ExerciseFrequency.THREE_PLUS_PER_WEEK,
            typicalDuration = TypicalDuration.OVER_60,
            experienceLevel = ExperienceLevel.EXPERIENCED,
            ascentExperience = AscentExperience.OVER_800,
            heightCm = 178,
            weightKg = 70,
            commonPackWeightKg = 6
        )
        val route = ImportedRoute(
            routeName = "Short Ridge",
            fileName = "short-ridge.gpx",
            distanceKm = 6.0,
            ascentMeters = 220,
            status = RouteImportStatus.PARSED,
            pointCount = 48
        )

        val assessment = RouteAssessmentEngine.assess(profile = profile, route = route)

        assertEquals(MatchLevel.RECOMMENDED, assessment.matchLevel)
        assertTrue(assessment.estimatedDurationRange.contains("-"))
    }

    @Test
    fun historicalGpxEvidenceRaisesConfidenceAndUsesHistoricalCapacity() {
        val assessment = RouteAssessmentEngine.assess(
            profile = TrailMateSampleData.baselineProfile,
            route = TrailMateSampleData.importedTargetRoute,
            historicalActivities = TrailMateSampleData.historicalActivities
        )

        assertEquals(ConfidenceLevel.MEDIUM, assessment.confidenceLevel)
        assertEquals(MatchLevel.RECOMMENDED, assessment.matchLevel)
        assertTrue(assessment.risks.any { it.contains("Historical GPX evidence", ignoreCase = true) })
        assertFalse(assessment.risks.any { it.contains("Confidence stays LOW", ignoreCase = true) })
    }

    @Test
    fun historicalGpxEvidenceCanLowerOptimisticQuestionnaireCapacity() {
        val optimisticProfile = BaselineProfile(
            exerciseFrequency = ExerciseFrequency.THREE_PLUS_PER_WEEK,
            typicalDuration = TypicalDuration.OVER_60,
            experienceLevel = ExperienceLevel.EXPERIENCED,
            ascentExperience = AscentExperience.OVER_800,
            heightCm = 178,
            weightKg = 70,
            commonPackWeightKg = 6
        )
        val moderateRoute = ImportedRoute(
            routeName = "Moderate Ridge",
            fileName = "moderate-ridge.gpx",
            distanceKm = 8.0,
            ascentMeters = 300,
            status = RouteImportStatus.PARSED,
            pointCount = 40
        )
        val shortHistory = listOf(
            HistoricalActivity("Lunch Loop", distanceKm = 3.8, ascentMeters = 120, durationMinutes = 70),
            HistoricalActivity("Creek Walk", distanceKm = 4.4, ascentMeters = 160, durationMinutes = 82),
            HistoricalActivity("Hill Repeat", distanceKm = 5.0, ascentMeters = 200, durationMinutes = 105)
        )

        val assessment = RouteAssessmentEngine.assess(
            profile = optimisticProfile,
            route = moderateRoute,
            historicalActivities = shortHistory
        )

        assertEquals(ConfidenceLevel.MEDIUM, assessment.confidenceLevel)
        assertEquals(MatchLevel.NOT_RECOMMENDED, assessment.matchLevel)
        assertTrue(assessment.risks.any { it.contains("historical GPX stable range (5 km)") })
        assertTrue(assessment.risks.any { it.contains("historical GPX ascent range (+200 m)") })
    }

    @Test
    fun flatHistoricalGpxEvidenceKeepsAssessmentFinite() {
        val flatRoute = ImportedRoute(
            routeName = "Park Connector",
            fileName = "park-connector.gpx",
            distanceKm = 1.5,
            ascentMeters = 0,
            status = RouteImportStatus.PARSED,
            pointCount = 24
        )
        val flatHistory = listOf(
            HistoricalActivity("Canal Walk", distanceKm = 1.2, ascentMeters = 0, durationMinutes = 24),
            HistoricalActivity("River Path", distanceKm = 1.8, ascentMeters = 0, durationMinutes = 34),
            HistoricalActivity("Park Loop", distanceKm = 1.5, ascentMeters = 0, durationMinutes = 28)
        )

        val assessment = RouteAssessmentEngine.assess(
            profile = TrailMateSampleData.baselineProfile,
            route = flatRoute,
            historicalActivities = flatHistory
        )

        assertEquals(ConfidenceLevel.MEDIUM, assessment.confidenceLevel)
        assertEquals(MatchLevel.RECOMMENDED, assessment.matchLevel)
        assertFalse(assessment.estimatedDurationRange.contains("NaN"))
        assertFalse(assessment.estimatedDurationRange.contains("Infinity"))
        assertTrue(assessment.risks.any { it.contains("Historical GPX evidence", ignoreCase = true) })
    }

    @Test
    fun gearAdvisorCannotChangeDeterministicAssessment() {
        val assessment = RouteAssessmentEngine.assess(
            profile = TrailMateSampleData.baselineProfile,
            route = TrailMateSampleData.importedTargetRoute
        )

        GearInventory(TrailMateSampleData.gearItems)
            .addBrandGear("Trekking poles", "Leki", "Makalu Lite", 510)
            .applyTo(TrailMateSampleData.gearRecommendations)

        assertEquals(MatchLevel.CAUTION, assessment.matchLevel)
        assertEquals(ConfidenceLevel.LOW, assessment.confidenceLevel)
        assertEquals("Longjing Ridge", assessment.routeName)
    }
}
