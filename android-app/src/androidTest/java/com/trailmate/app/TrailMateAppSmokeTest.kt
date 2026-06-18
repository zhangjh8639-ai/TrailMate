package com.trailmate.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.trailmate.app.core.design.TrailMateTheme
import com.trailmate.app.core.model.AiGearAdvisorResponse
import com.trailmate.app.core.model.BaselineProfile
import com.trailmate.app.core.gpx.GpxImportJobKind
import com.trailmate.app.core.gpx.GpxImportJobStatus
import com.trailmate.app.core.gpx.GpxImportQueue
import com.trailmate.app.core.model.ExerciseFrequency
import com.trailmate.app.core.model.ExperienceLevel
import com.trailmate.app.core.model.GearInventory
import com.trailmate.app.core.model.GearItem
import com.trailmate.app.core.model.GearRecommendation
import com.trailmate.app.core.model.GearStatus
import com.trailmate.app.core.model.HistoricalActivity
import com.trailmate.app.core.model.ImportedRoute
import com.trailmate.app.core.model.AscentExperience
import com.trailmate.app.core.model.TrailMateSampleData
import com.trailmate.app.core.model.TypicalDuration
import com.trailmate.app.core.persistence.TrailMateSessionRepository
import com.trailmate.app.core.persistence.TrailMateSnapshot
import com.trailmate.app.feature.gear.MyGearScreen
import com.trailmate.app.feature.home.HomeScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TrailMateAppSmokeTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun showsTrailMateOnboarding() {
        compose.setContent {
            TrailMateTheme {
                TrailMateApp(sessionRepository = FakeTrailMateSessionRepository())
            }
        }

        compose.onNodeWithText("TrailMate").assertExists()
        compose.onNodeWithText("Start baseline profile").assertExists()
    }

    @Test
    fun onboardingCollectsBaselineProfileBeforeHome() {
        compose.setContent {
            TrailMateTheme {
                TrailMateApp(sessionRepository = FakeTrailMateSessionRepository())
            }
        }

        compose.onNodeWithText("Start baseline profile").performClick()
        compose.onNodeWithText("Height cm").performScrollTo().performTextInput("181")
        compose.onNodeWithText("Weight kg").performScrollTo().performTextInput("76")
        compose.onNodeWithText("Usual pack kg").performScrollTo().performTextInput("7")
        compose.onNodeWithText("Save profile").performScrollTo().performClick()

        compose.onNodeWithText("Trail coach").assertExists()
        compose.onNodeWithText("181cm / 76kg").assertExists()
        compose.onNodeWithText("7 kg pack").assertExists()
    }

    @Test
    fun appRestoresSavedProfileToHome() {
        compose.setContent {
            TrailMateTheme {
                TrailMateApp(
                    sessionRepository = FakeTrailMateSessionRepository(
                        TrailMateSnapshot(
                            profile = savedProfile(),
                            historicalActivities = TrailMateSampleData.historicalActivities
                        )
                    )
                )
            }
        }

        compose.onNodeWithText("Trail coach").assertExists()
        compose.onNodeWithText("181cm / 76kg").assertExists()
        compose.onNodeWithText("3/3 GPX").assertExists()
        compose.onNodeWithText("Historical profile").assertExists()
        compose.onAllNodesWithText("Start baseline profile").assertCountEquals(0)
    }

    @Test
    fun onboardingSavePersistsProfile() {
        val store = FakeTrailMateSessionRepository()

        compose.setContent {
            TrailMateTheme {
                TrailMateApp(sessionRepository = store)
            }
        }

        compose.onNodeWithText("Start baseline profile").performClick()
        compose.onNodeWithText("Height cm").performScrollTo().performTextInput("181")
        compose.onNodeWithText("Weight kg").performScrollTo().performTextInput("76")
        compose.onNodeWithText("Usual pack kg").performScrollTo().performTextInput("7")
        compose.onNodeWithText("Save profile").performScrollTo().performClick()

        assertEquals(181, store.snapshot.profile?.heightCm)
        assertEquals(76, store.snapshot.profile?.weightKg)
        assertEquals(7, store.snapshot.profile?.commonPackWeightKg)
    }

    @Test
    fun onboardingSkipDoesNotFabricateBodyMetrics() {
        compose.setContent {
            TrailMateTheme {
                TrailMateApp(sessionRepository = FakeTrailMateSessionRepository())
            }
        }

        compose.onNodeWithText("Start baseline profile").performClick()
        compose.onNodeWithText("Skip for now").performScrollTo().performClick()

        compose.onNodeWithText("Trail coach").assertExists()
        compose.onNodeWithText("Not set").assertExists()
        compose.onNodeWithText("Pack TBD").assertExists()
        compose.onAllNodesWithText("172cm / 68kg").assertCountEquals(0)
    }

    @Test
    fun routeDetailShowsAssessmentRoutePlanAndGearTabs() {
        compose.setContent {
            TrailMateTheme {
                com.trailmate.app.feature.route.RouteDetailScreen()
            }
        }

        compose.onNodeWithText("Assessment").assertExists()
        compose.onNodeWithText("Route").assertExists()
        compose.onNodeWithText("Plan").assertExists()
        compose.onNodeWithText("Gear").assertExists()
    }

    @Test
    fun myGearShowsOwnedBrandGearAndAddAction() {
        val inventory = GearInventory(TrailMateSampleData.gearItems)

        compose.setContent {
            TrailMateTheme {
                MyGearScreen(
                    inventory = inventory,
                    routeGearRecommendations = inventory.applyTo(TrailMateSampleData.gearRecommendations),
                    requestedCategory = "Trekking poles",
                    onAddBrandGear = { _, _, _, _ -> },
                    onSetAvailability = { _, _ -> },
                    onDeleteGear = {}
                )
            }
        }

        compose.onNodeWithText("My Gear").assertExists()
        compose.onNodeWithText("Add brand gear").assertExists()
        compose.onNodeWithText("Salomon X Ultra 4 GTX").assertExists()
        compose.onAllNodesWithText("Available").assertCountEquals(3)
        compose.onAllNodesWithText("Remove").assertCountEquals(3)
    }

    @Test
    fun myGearDetailsTabShowsSelectedGearRouteContext() {
        val inventory = GearInventory(
            items = listOf(
                GearItem(
                    id = "shell-1",
                    category = "Rain shell",
                    brand = "Patagonia",
                    model = "Torrentshell",
                    weightGrams = 400,
                    available = true
                )
            )
        )
        val recommendations = inventory.applyTo(
            listOf(
                GearRecommendation(
                    category = "Rain shell",
                    status = GearStatus.MISSING,
                    rationale = "Existing shell covers wind and light rain."
                )
            )
        )

        compose.setContent {
            TrailMateTheme {
                MyGearScreen(
                    inventory = inventory,
                    routeGearRecommendations = recommendations,
                    requestedCategory = "",
                    onAddBrandGear = { _, _, _, _ -> },
                    onSetAvailability = { _, _ -> },
                    onDeleteGear = {}
                )
            }
        }

        compose.onNodeWithText("View details").performClick()

        compose.onNodeWithText("Gear details").assertExists()
        compose.onNodeWithText("Patagonia Torrentshell").assertExists()
        compose.onNodeWithText("400g / ready").assertExists()
        compose.onNodeWithText("Matches Rain shell recommendation.").assertExists()
        compose.onNodeWithText("Existing shell covers wind and light rain.").assertExists()
    }

    @Test
    fun routeGearTabShowsMatchedOwnedGear() {
        compose.setContent {
            TrailMateTheme {
                com.trailmate.app.feature.route.RouteDetailScreen()
            }
        }

        compose.onNodeWithText("Gear").performClick()
        compose.onNodeWithText("AI advisor").assertExists()
        compose.onNodeWithText("Fallback active").assertExists()
        compose.onNodeWithText("Rain shell").assertExists()
        compose.onNodeWithText("Matched with Patagonia Torrentshell.", substring = true).assertExists()
        compose.onNodeWithText("Extra water").assertExists()
        compose.onNodeWithText("Add Trekking poles to My Gear").assertExists()
    }

    @Test
    fun routeGearTabMarksStaleAiResponseAndKeepsFallbackChecklist() {
        compose.setContent {
            TrailMateTheme {
                com.trailmate.app.feature.route.RouteDetailScreen(
                    aiGearAdvisorResponse = AiGearAdvisorResponse(
                        assessmentFingerprint = "old-route",
                        recommendations = listOf(
                            GearRecommendation(
                                category = "Avalanche beacon",
                                status = GearStatus.MISSING,
                                rationale = "Old winter route response."
                            )
                        )
                    )
                )
            }
        }

        compose.onNodeWithText("Gear").performClick()

        compose.onNodeWithText("Stale response").assertExists()
        compose.onNodeWithText("different route", substring = true).assertExists()
        compose.onNodeWithText("Rain shell").assertExists()
        compose.onAllNodesWithText("Avalanche beacon").assertCountEquals(0)
    }

    @Test
    fun routePlanTabShowsDynamicCheckpoints() {
        compose.setContent {
            TrailMateTheme {
                com.trailmate.app.feature.route.RouteDetailScreen()
            }
        }

        compose.onNodeWithText("Plan").performClick()

        compose.onNodeWithText("Energy check").assertExists()
        compose.onNodeWithText("Risk check").assertExists()
        compose.onNodeWithText("15.2 km /", substring = true).assertExists()
        compose.onAllNodesWithText("Plan checkpoints").assertCountEquals(0)
    }

    @Test
    fun routeTabStartsAndAdvancesActiveHike() {
        compose.setContent {
            TrailMateTheme {
                com.trailmate.app.feature.route.RouteDetailScreen()
            }
        }

        compose.onNodeWithText("Route").performClick()
        compose.onNodeWithText("Start hike").performClick()

        compose.onNodeWithText("Pause").assertExists()
        compose.onNodeWithText("Mark next checkpoint").assertExists()
        compose.onNodeWithText("Energy check").assertExists()

        compose.onNodeWithText("Mark next checkpoint").performClick()
        compose.onNodeWithText("Rest check").assertExists()
    }

    @Test
    fun activeHikeResetsWhenRouteChanges() {
        var route by mutableStateOf(TrailMateSampleData.importedTargetRoute)

        compose.setContent {
            TrailMateTheme {
                com.trailmate.app.feature.route.RouteDetailScreen(route = route)
            }
        }

        compose.onNodeWithText("Route").performClick()
        compose.onNodeWithText("Start hike").performClick()
        compose.onNodeWithText("Pause").assertExists()

        compose.runOnIdle {
            route = route.copy(
                routeName = "Replacement loop",
                fileName = "replacement.gpx",
                distanceKm = 4.8,
                ascentMeters = 180,
                pointCount = 64
            )
        }

        compose.onNodeWithText("Replacement loop").assertExists()
        compose.onNodeWithText("Start hike").assertExists()
        compose.onAllNodesWithText("Pause").assertCountEquals(0)
    }

    @Test
    fun homeGearAddFlowPrefillsCategoryAndUpdatesRouteMatch() {
        compose.setContent {
            TrailMateTheme {
                HomeScreen()
            }
        }

        compose.onNodeWithText("Use sample GPX").performClick()
        compose.onNodeWithText("Gear").performClick()
        compose.onNodeWithText("Add Trekking poles to My Gear").performClick()
        compose.onNodeWithText("My Gear").assertExists()
        compose.onNodeWithText("Trekking poles").assertExists()

        compose.onNodeWithText("Brand").performTextInput("Leki")
        compose.onNodeWithText("Model").performTextInput("Makalu Lite")
        compose.onNodeWithText("Add to My Gear").performClick()
        compose.onNodeWithText("Route").performClick()
        compose.onNodeWithText("Gear").performClick()

        compose.onNodeWithText("Matched with Leki Makalu Lite.", substring = true).assertExists()
    }

    @Test
    fun homeNotifiesPersistenceWhenRouteAndGearChange() {
        var savedRoute: ImportedRoute? = null
        var savedInventory: GearInventory? = null
        val savedQueues = mutableListOf<GpxImportQueue>()

        compose.setContent {
            TrailMateTheme {
                HomeScreen(
                    profile = TrailMateSampleData.baselineProfile,
                    onRouteImported = { route -> savedRoute = route },
                    onInventoryChanged = { inventory -> savedInventory = inventory },
                    onGpxImportQueueChanged = { queue -> savedQueues += queue }
                )
            }
        }

        compose.onNodeWithText("Use sample GPX").performClick()
        compose.onNodeWithText("Gear").performClick()
        compose.onNodeWithText("Add Trekking poles to My Gear").performClick()
        compose.onNodeWithText("Brand").performTextInput("Leki")
        compose.onNodeWithText("Model").performTextInput("Makalu Lite")
        compose.onNodeWithText("Add to My Gear").performClick()

        assertEquals("Longjing Ridge", savedRoute?.routeName)
        assertTrue(savedInventory?.items.orEmpty().any { item ->
            item.category == "Trekking poles" && item.brand == "Leki"
        })
        assertTrue(savedQueues.any { queue ->
            queue.jobs.any { job ->
                job.kind == GpxImportJobKind.TARGET_ROUTE &&
                    job.fileName == "longjing-ridge-target.gpx" &&
                    job.status == GpxImportJobStatus.RUNNING
            }
        })
        assertTrue(savedQueues.any { queue ->
            queue.jobs.any { job ->
                job.kind == GpxImportJobKind.TARGET_ROUTE &&
                    job.fileName == "longjing-ridge-target.gpx" &&
                    job.status == GpxImportJobStatus.SUCCEEDED
            }
        })
    }

    @Test
    fun dataTabShowsExportPreviewAndClearsLocalData() {
        val initialSnapshot = TrailMateSnapshot(
            profile = savedProfile(),
            inventory = GearInventory(TrailMateSampleData.gearItems),
            importedRoute = TrailMateSampleData.importedTargetRoute
        )
        val store = FakeTrailMateSessionRepository(
            initialSnapshot
        )

        compose.setContent {
            TrailMateTheme {
                TrailMateApp(sessionRepository = store)
            }
        }

        compose.onNodeWithText("Data").performClick()
        compose.onNodeWithText("Local data").assertExists()
        compose.onNodeWithText("Profile saved").assertExists()
        compose.onNodeWithText("Longjing Ridge / 15.2 km / +860 m").assertExists()
        compose.onNodeWithText("3 items / 3 ready").assertExists()
        compose.onNodeWithText("Export preview").assertExists()
        compose.onNodeWithText("Profile: saved", substring = true).assertExists()
        compose.onNodeWithText("Route: Longjing Ridge, 15.2 km, +860 m", substring = true).assertExists()
        compose.onNodeWithText("History: 0 GPX activities", substring = true).assertExists()
        compose.onNodeWithText("Gear: 3 items, 3 ready", substring = true).assertExists()

        compose.onNodeWithText("Clear local data").performClick()

        compose.onNodeWithText("Clear local data?").assertExists()
        compose.onNodeWithText("Confirm clear data").assertExists()
        compose.onNodeWithText("Cancel").assertExists()
        assertEquals(initialSnapshot, store.snapshot)

        compose.onNodeWithText("Cancel").performClick()
        compose.onAllNodesWithText("Confirm clear data").assertCountEquals(0)

        compose.onNodeWithText("Clear local data").performClick()
        compose.onNodeWithText("Confirm clear data").performClick()

        assertEquals(TrailMateSnapshot.empty(), store.snapshot)
        compose.onNodeWithText("TrailMate").assertExists()
        compose.onNodeWithText("Start baseline profile").assertExists()

        compose.onNodeWithText("Start baseline profile").performClick()
        compose.onNodeWithText("Skip for now").performScrollTo().performClick()
        compose.onNodeWithText("Data").performClick()

        compose.onNodeWithText("No route imported").assertExists()
        compose.onNodeWithText("0 items / 0 ready").assertExists()
        compose.onAllNodesWithText("Longjing Ridge / 15.2 km / +860 m").assertCountEquals(0)
    }

    @Test
    fun homeRequiresRouteImportBeforeShowingRouteDetail() {
        compose.setContent {
            TrailMateTheme {
                HomeScreen()
            }
        }

        compose.onNodeWithText("Import GPX").assertExists()
        compose.onNodeWithText("Import queue").assertExists()
        compose.onNodeWithText("Ready for GPX").assertExists()
        compose.onNodeWithText("Choose GPX file").assertExists()
        compose.onAllNodesWithText("15.2 km").assertCountEquals(0)
        compose.onAllNodesWithText("Assessment").assertCountEquals(0)

        compose.onNodeWithText("Use sample GPX").performClick()

        compose.onNodeWithText("Imported GPX").assertExists()
        compose.onNodeWithText("Parsed").assertExists()
        compose.onNodeWithText("3 points", substring = true).assertExists()
        compose.onNodeWithText("Assessment").assertExists()
        compose.onNodeWithText("CAUTION").assertExists()
        compose.onNodeWithText("Gear").assertExists()
    }

    @Test
    fun homeCanApplySampleHistoricalGpxEvidence() {
        var savedHistory = emptyList<HistoricalActivity>()

        compose.setContent {
            TrailMateTheme {
                HomeScreen(
                    onHistoricalActivitiesChanged = { activities ->
                        savedHistory = activities
                    }
                )
            }
        }

        compose.onNodeWithText("0/3 GPX").assertExists()
        compose.onNodeWithText("History import").assertExists()
        compose.onNodeWithText("Ready for history GPX").assertExists()
        compose.onNodeWithText("Choose history GPX").assertExists()
        compose.onNodeWithText("Use sample history").performClick()

        compose.onNodeWithText("Historical profile").assertExists()
        compose.onNodeWithText("MEDIUM").assertExists()
        compose.onNodeWithText("3/3 GPX").assertExists()
        compose.onNodeWithText("Longest 18.6 km / +980 m").assertExists()
        compose.onNodeWithText("Average 13.7 km / +720 m", substring = true).assertExists()

        compose.onNodeWithText("Use sample GPX").performClick()

        compose.onNodeWithText("Recommended").assertExists()
        compose.onNodeWithText("RECOMMENDED").assertExists()
        compose.onNodeWithText("Historical GPX evidence", substring = true).assertExists()
        assertEquals(TrailMateSampleData.historicalActivities, savedHistory)
    }

    @Test
    fun homeShowsHistoricalActivitiesAndCanRemoveOne() {
        var savedHistory = emptyList<HistoricalActivity>()

        compose.setContent {
            TrailMateTheme {
                HomeScreen(
                    onHistoricalActivitiesChanged = { activities ->
                        savedHistory = activities
                    }
                )
            }
        }

        compose.onNodeWithText("Use sample history").performClick()

        compose.onNodeWithText("History activities").assertExists()
        compose.onNodeWithText("Morning Ridge Loop").assertExists()
        compose.onNodeWithText("9.8 km / +420 m / 2:45").assertExists()
        compose.onAllNodesWithText("Remove history").assertCountEquals(3)

        compose.onAllNodesWithText("Remove history").onFirst().performClick()

        compose.onAllNodesWithText("Morning Ridge Loop").assertCountEquals(0)
        compose.onAllNodesWithText("Remove history").assertCountEquals(2)
        assertEquals(2, savedHistory.size)
        assertFalse(savedHistory.any { activity -> activity.routeName == "Morning Ridge Loop" })
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

    private class FakeTrailMateSessionRepository(
        initialSnapshot: TrailMateSnapshot = TrailMateSnapshot()
    ) : TrailMateSessionRepository {
        var snapshot: TrailMateSnapshot = initialSnapshot
            private set

        override fun loadSnapshot(): TrailMateSnapshot = snapshot

        override fun saveProfile(profile: BaselineProfile) {
            snapshot = snapshot.copy(profile = profile)
        }

        override fun saveInventory(inventory: GearInventory) {
            snapshot = snapshot.copy(inventory = inventory)
        }

        override fun saveImportedRoute(route: ImportedRoute) {
            snapshot = snapshot.copy(importedRoute = route)
        }

        override fun saveHistoricalActivities(historicalActivities: List<HistoricalActivity>) {
            snapshot = snapshot.copy(historicalActivities = historicalActivities)
        }

        override fun saveGpxImportQueue(queue: GpxImportQueue) {
            snapshot = snapshot.copy(gpxImportQueue = queue)
        }

        override fun clearLocalData() {
            snapshot = TrailMateSnapshot.empty()
        }
    }
}
