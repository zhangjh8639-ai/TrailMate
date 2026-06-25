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

        assertEquals("龙井山脊", assessment.routeName)
        assertEquals(15.2, assessment.distanceKm, 0.0)
        assertEquals(860, assessment.ascentMeters)
        assertEquals(ConfidenceLevel.LOW, assessment.confidenceLevel)
        assertEquals(MatchLevel.CAUTION, assessment.matchLevel)
        assertTrue(assessment.risks.any { it.contains("爬升") })
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
        assertTrue(assessment.risks.any { it.contains("历史活动覆盖到") })
        assertFalse(assessment.risks.any { it.contains("更保守的节奏") })
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
        assertTrue(assessment.risks.any { it.contains("历史 GPX稳定范围（5 km）") })
        assertTrue(assessment.risks.any { it.contains("历史 GPX稳定范围（+200 m）") })
    }

    @Test
    fun historicalGpxDurationCalibratesEstimatedDuration() {
        val route = ImportedRoute(
            routeName = "Slow Fit Check",
            fileName = "slow-fit-check.gpx",
            distanceKm = 6.0,
            ascentMeters = 100,
            status = RouteImportStatus.PARSED,
            pointCount = 32
        )
        val slowHistory = listOf(
            HistoricalActivity("Slow Loop A", distanceKm = 5.0, ascentMeters = 100, durationMinutes = 180),
            HistoricalActivity("Slow Loop B", distanceKm = 5.0, ascentMeters = 100, durationMinutes = 180),
            HistoricalActivity("Slow Loop C", distanceKm = 5.0, ascentMeters = 100, durationMinutes = 180)
        )

        val assessment = RouteAssessmentEngine.assess(
            profile = TrailMateSampleData.baselineProfile,
            route = route,
            historicalActivities = slowHistory
        )

        assertEquals("3:13-4:17", assessment.estimatedDurationRange)
    }

    @Test
    fun unusableHistoricalDurationsDoNotProduceInvalidEta() {
        val route = ImportedRoute(
            routeName = "Duration Guard",
            fileName = "duration-guard.gpx",
            distanceKm = 6.0,
            ascentMeters = 100,
            status = RouteImportStatus.PARSED,
            pointCount = 32
        )
        val malformedHistory = listOf(
            HistoricalActivity("Bad A", distanceKm = 5.0, ascentMeters = 100, durationMinutes = 0),
            HistoricalActivity("Bad B", distanceKm = 5.0, ascentMeters = 100, durationMinutes = 0),
            HistoricalActivity("Bad C", distanceKm = 5.0, ascentMeters = 100, durationMinutes = 0)
        )

        val assessment = RouteAssessmentEngine.assess(
            profile = TrailMateSampleData.baselineProfile,
            route = route,
            historicalActivities = malformedHistory
        )

        assertEquals(ConfidenceLevel.MEDIUM, assessment.confidenceLevel)
        assertFalse(assessment.estimatedDurationRange.contains("NaN"))
        assertFalse(assessment.estimatedDurationRange.contains("Infinity"))
        assertFalse(assessment.estimatedDurationRange.startsWith("0:00"))
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
        assertTrue(assessment.risks.any { it.contains("历史活动覆盖到") })
    }

    @Test
    fun gearAdvisorCannotChangeDeterministicAssessment() {
        val assessment = RouteAssessmentEngine.assess(
            profile = TrailMateSampleData.baselineProfile,
            route = TrailMateSampleData.importedTargetRoute
        )

        RouteGearAdvisorEngine.recommend(
            route = TrailMateSampleData.importedTargetRoute,
            assessment = assessment
        )

        assertEquals(MatchLevel.CAUTION, assessment.matchLevel)
        assertEquals(ConfidenceLevel.LOW, assessment.confidenceLevel)
        assertEquals("龙井山脊", assessment.routeName)
    }
}
