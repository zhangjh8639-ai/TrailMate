package com.trailmate.app.core.persistence

import com.trailmate.app.core.model.AscentExperience
import com.trailmate.app.core.model.BaselineProfile
import com.trailmate.app.core.model.ExerciseFrequency
import com.trailmate.app.core.model.ExperienceLevel
import com.trailmate.app.core.model.GearInventory
import com.trailmate.app.core.model.GearItem
import com.trailmate.app.core.model.HistoricalActivity
import com.trailmate.app.core.model.ImportedRoute
import com.trailmate.app.core.model.RouteImportStatus
import com.trailmate.app.core.model.TypicalDuration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TrailMateSnapshotCodecTest {
    @Test
    fun snapshotRoundTripsProfileInventoryAndImportedRoute() {
        val snapshot = TrailMateSnapshot(
            profile = BaselineProfile(
                exerciseFrequency = ExerciseFrequency.THREE_PLUS_PER_WEEK,
                typicalDuration = TypicalDuration.OVER_60,
                experienceLevel = ExperienceLevel.EXPERIENCED,
                ascentExperience = AscentExperience.OVER_800,
                heightCm = 181,
                weightKg = 76,
                commonPackWeightKg = 7
            ),
            inventory = GearInventory(
                items = listOf(
                    GearItem(
                        id = "warm-layer-1",
                        category = "Warm layer",
                        brand = "Rab",
                        model = "Xenair Alpine Light",
                        weightGrams = 309,
                        available = false
                    )
                )
            ),
            importedRoute = ImportedRoute(
                routeName = "West Ridge",
                fileName = "west-ridge.gpx",
                distanceKm = 8.4,
                ascentMeters = 540,
                status = RouteImportStatus.PARSED,
                pointCount = 64,
                durationMinutes = 128
            ),
            historicalActivities = listOf(
                HistoricalActivity(
                    routeName = "Old Ridge",
                    distanceKm = 11.2,
                    ascentMeters = 620,
                    durationMinutes = 240
                )
            )
        )

        val decoded = TrailMateSnapshotCodec.decode(TrailMateSnapshotCodec.encode(snapshot))

        assertEquals(snapshot, decoded)
    }

    @Test
    fun snapshotRoundTripPreservesUnsetBodyAndOptionalGearFields() {
        val snapshot = TrailMateSnapshot(
            profile = BaselineProfile(
                exerciseFrequency = ExerciseFrequency.RARELY,
                typicalDuration = TypicalDuration.UNDER_30,
                experienceLevel = ExperienceLevel.BEGINNER,
                ascentExperience = AscentExperience.UNDER_300,
                heightCm = null,
                weightKg = null,
                commonPackWeightKg = null
            ),
            inventory = GearInventory(
                items = listOf(
                    GearItem(
                        id = "warm-layer-1",
                        category = "Warm layer",
                        brand = null,
                        model = null,
                        weightGrams = null,
                        available = true
                    )
                )
            )
        )

        val decoded = TrailMateSnapshotCodec.decode(TrailMateSnapshotCodec.encode(snapshot))

        assertNull(decoded.profile?.heightCm)
        assertNull(decoded.profile?.weightKg)
        assertNull(decoded.profile?.commonPackWeightKg)
        assertNull(decoded.inventory.items.single().brand)
        assertNull(decoded.inventory.items.single().model)
        assertNull(decoded.inventory.items.single().weightGrams)
    }

    @Test
    fun blankSnapshotDecodesToPrototypeDefaults() {
        val decoded = TrailMateSnapshotCodec.decode("")

        assertNull(decoded.profile)
        assertEquals(3, decoded.inventory.items.size)
        assertNull(decoded.importedRoute)
    }

    @Test
    fun explicitEmptySnapshotRoundTripsWithoutPrototypeDefaults() {
        val decoded = TrailMateSnapshotCodec.decode(
            TrailMateSnapshotCodec.encode(TrailMateSnapshot.empty())
        )

        assertNull(decoded.profile)
        assertEquals(0, decoded.inventory.items.size)
        assertNull(decoded.importedRoute)
        assertEquals(0, decoded.historicalActivities.size)
    }
}
