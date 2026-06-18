package com.trailmate.app

import com.trailmate.app.core.model.AscentExperience
import com.trailmate.app.core.model.BaselineProfile
import com.trailmate.app.core.model.ExerciseFrequency
import com.trailmate.app.core.model.ExperienceLevel
import com.trailmate.app.core.model.GearInventory
import com.trailmate.app.core.model.TrailMateSampleData
import com.trailmate.app.core.model.TypicalDuration
import com.trailmate.app.core.persistence.TrailMateSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TrailMateAppSessionTest {
    @Test
    fun profileSavedAfterClearKeepsRouteAndGearEmpty() {
        val previousSession = TrailMateAppSession(
            TrailMateSnapshot(
                profile = savedProfile(),
                inventory = GearInventory(TrailMateSampleData.gearItems),
                importedRoute = TrailMateSampleData.importedTargetRoute,
                historicalActivities = TrailMateSampleData.historicalActivities
            )
        )

        val nextSession = previousSession.clear().withProfile(savedProfile())

        assertEquals(savedProfile(), nextSession.snapshot.profile)
        assertEquals(emptyList<Nothing>(), nextSession.snapshot.inventory.items)
        assertNull(nextSession.snapshot.importedRoute)
        assertEquals(emptyList<Nothing>(), nextSession.snapshot.historicalActivities)
    }

    @Test
    fun historicalActivitiesArePartOfSessionSnapshot() {
        val nextSession = TrailMateAppSession(TrailMateSnapshot.empty())
            .withHistoricalActivities(TrailMateSampleData.historicalActivities)

        assertEquals(TrailMateSampleData.historicalActivities, nextSession.snapshot.historicalActivities)
    }

    private fun savedProfile(): BaselineProfile =
        BaselineProfile(
            exerciseFrequency = ExerciseFrequency.THREE_PLUS_PER_WEEK,
            typicalDuration = TypicalDuration.OVER_60,
            experienceLevel = ExperienceLevel.EXPERIENCED,
            ascentExperience = AscentExperience.OVER_800,
            heightCm = 181,
            weightKg = 76,
            commonPackWeightKg = 7
        )
}
