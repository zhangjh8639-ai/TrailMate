package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BaselineProfileRulesTest {
    @Test
    fun questionnaireProfileStartsWithLowConfidence() {
        val profile = BaselineProfile(
            exerciseFrequency = ExerciseFrequency.ONE_TO_TWO_PER_WEEK,
            typicalDuration = TypicalDuration.MIN_30_TO_60,
            experienceLevel = ExperienceLevel.REGULAR,
            ascentExperience = AscentExperience.M300_TO_800,
            heightCm = 172,
            weightKg = 68,
            commonPackWeightKg = 5
        )

        assertEquals(ConfidenceLevel.LOW, profile.initialConfidence())
        assertTrue(profile.explanation().contains("GPX"))
    }

    @Test
    fun questionnaireProfileFormatsBodyAndPackSummary() {
        val profile = TrailMateSampleData.baselineProfile

        assertEquals("172cm / 68kg", profile.bodyMetricsLabel())
        assertEquals("背包 5 kg", profile.packWeightLabel())
    }
}
