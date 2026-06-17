package com.trailmate.app

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.trailmate.app.core.design.TrailMateTheme
import com.trailmate.app.core.model.BaselineProfile
import com.trailmate.app.core.model.ExerciseFrequency
import com.trailmate.app.core.model.ExperienceLevel
import com.trailmate.app.core.model.GearInventory
import com.trailmate.app.core.model.ImportedRoute
import com.trailmate.app.core.model.AscentExperience
import com.trailmate.app.core.model.TrailMateSampleData
import com.trailmate.app.core.model.TypicalDuration
import com.trailmate.app.core.persistence.TrailMateSessionStore
import com.trailmate.app.core.persistence.TrailMateSnapshot
import com.trailmate.app.feature.gear.MyGearScreen
import com.trailmate.app.feature.home.HomeScreen
import org.junit.Assert.assertEquals
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
                TrailMateApp(sessionStore = FakeTrailMateSessionStore())
            }
        }

        compose.onNodeWithText("TrailMate").assertExists()
        compose.onNodeWithText("Start baseline profile").assertExists()
    }

    @Test
    fun onboardingCollectsBaselineProfileBeforeHome() {
        compose.setContent {
            TrailMateTheme {
                TrailMateApp(sessionStore = FakeTrailMateSessionStore())
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
                    sessionStore = FakeTrailMateSessionStore(
                        TrailMateSnapshot(profile = savedProfile())
                    )
                )
            }
        }

        compose.onNodeWithText("Trail coach").assertExists()
        compose.onNodeWithText("181cm / 76kg").assertExists()
        compose.onAllNodesWithText("Start baseline profile").assertCountEquals(0)
    }

    @Test
    fun onboardingSavePersistsProfile() {
        val store = FakeTrailMateSessionStore()

        compose.setContent {
            TrailMateTheme {
                TrailMateApp(sessionStore = store)
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
                TrailMateApp(sessionStore = FakeTrailMateSessionStore())
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
    fun routeGearTabShowsMatchedOwnedGear() {
        compose.setContent {
            TrailMateTheme {
                com.trailmate.app.feature.route.RouteDetailScreen()
            }
        }

        compose.onNodeWithText("Gear").performClick()
        compose.onNodeWithText("Rain shell").assertExists()
        compose.onNodeWithText("Matched with Patagonia Torrentshell.", substring = true).assertExists()
        compose.onNodeWithText("Extra water").assertExists()
        compose.onNodeWithText("Add Trekking poles to My Gear").assertExists()
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

        compose.setContent {
            TrailMateTheme {
                HomeScreen(
                    profile = TrailMateSampleData.baselineProfile,
                    onRouteImported = { route -> savedRoute = route },
                    onInventoryChanged = { inventory -> savedInventory = inventory }
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
    }

    @Test
    fun homeRequiresRouteImportBeforeShowingRouteDetail() {
        compose.setContent {
            TrailMateTheme {
                HomeScreen()
            }
        }

        compose.onNodeWithText("Import GPX").assertExists()
        compose.onNodeWithText("Choose GPX file").assertExists()
        compose.onAllNodesWithText("15.2 km").assertCountEquals(0)
        compose.onAllNodesWithText("Assessment").assertCountEquals(0)

        compose.onNodeWithText("Use sample GPX").performClick()

        compose.onNodeWithText("Imported GPX").assertExists()
        compose.onNodeWithText("3 points", substring = true).assertExists()
        compose.onNodeWithText("Assessment").assertExists()
        compose.onNodeWithText("CAUTION").assertExists()
        compose.onNodeWithText("Gear").assertExists()
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

    private class FakeTrailMateSessionStore(
        initialSnapshot: TrailMateSnapshot = TrailMateSnapshot()
    ) : TrailMateSessionStore {
        var snapshot: TrailMateSnapshot = initialSnapshot
            private set

        override fun load(): TrailMateSnapshot = snapshot

        override fun saveProfile(profile: BaselineProfile) {
            snapshot = snapshot.copy(profile = profile)
        }

        override fun saveInventory(inventory: GearInventory) {
            snapshot = snapshot.copy(inventory = inventory)
        }

        override fun saveImportedRoute(route: ImportedRoute) {
            snapshot = snapshot.copy(importedRoute = route)
        }
    }
}
