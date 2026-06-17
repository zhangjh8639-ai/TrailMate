package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
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
