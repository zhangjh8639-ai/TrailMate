package com.trailmate.app

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.trailmate.app.core.design.TrailMateTheme
import com.trailmate.app.core.model.GearInventory
import com.trailmate.app.core.model.TrailMateSampleData
import com.trailmate.app.feature.gear.MyGearScreen
import com.trailmate.app.feature.home.HomeScreen
import org.junit.Rule
import org.junit.Test

class TrailMateAppSmokeTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun showsTrailMateOnboarding() {
        compose.setContent {
            TrailMateTheme {
                TrailMateApp()
            }
        }

        compose.onNodeWithText("TrailMate").assertExists()
        compose.onNodeWithText("Start baseline profile").assertExists()
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
        compose.onNodeWithText("Add Trekking poles to My Gear").assertExists()
    }

    @Test
    fun homeGearAddFlowPrefillsCategoryAndUpdatesRouteMatch() {
        compose.setContent {
            TrailMateTheme {
                HomeScreen()
            }
        }

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
}
