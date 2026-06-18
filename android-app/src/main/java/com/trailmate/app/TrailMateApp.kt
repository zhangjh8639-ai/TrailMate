package com.trailmate.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.trailmate.app.core.model.AscentExperience
import com.trailmate.app.core.model.BaselineProfile
import com.trailmate.app.core.model.ExerciseFrequency
import com.trailmate.app.core.model.ExperienceLevel
import com.trailmate.app.core.model.TrailMateSampleData
import com.trailmate.app.core.model.TypicalDuration
import com.trailmate.app.core.persistence.SharedPreferencesTrailMateSessionStore
import com.trailmate.app.core.persistence.TrailMateSessionStore
import com.trailmate.app.feature.home.HomeScreen
import com.trailmate.app.feature.onboarding.OnboardingScreen

enum class TrailMateScreen {
    ONBOARDING,
    HOME
}

@Composable
fun TrailMateApp(sessionStore: TrailMateSessionStore? = null) {
    val defaultSessionStore = rememberTrailMateSessionStore()
    val activeSessionStore = sessionStore ?: defaultSessionStore
    val initialSnapshot = remember(activeSessionStore) { activeSessionStore.load() }
    var appSession by remember(activeSessionStore) {
        mutableStateOf(TrailMateAppSession(initialSnapshot))
    }
    var screen by rememberSaveable {
        mutableStateOf(
            if (appSession.hasProfile) {
                TrailMateScreen.HOME
            } else {
                TrailMateScreen.ONBOARDING
            }
        )
    }
    var baselineProfile by rememberSaveable(stateSaver = BaselineProfileStateSaver) {
        mutableStateOf(appSession.baselineProfile)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (screen) {
            TrailMateScreen.ONBOARDING -> OnboardingScreen(
                onComplete = { profile ->
                    appSession = appSession.withProfile(profile)
                    baselineProfile = profile
                    activeSessionStore.saveProfile(profile)
                    screen = TrailMateScreen.HOME
                }
            )
            TrailMateScreen.HOME -> HomeScreen(
                profile = baselineProfile,
                initialInventory = appSession.snapshot.inventory,
                initialImportedRoute = appSession.snapshot.importedRoute,
                onInventoryChanged = { inventory ->
                    appSession = appSession.withInventory(inventory)
                    activeSessionStore.saveInventory(inventory)
                },
                onRouteImported = { route ->
                    appSession = appSession.withImportedRoute(route)
                    activeSessionStore.saveImportedRoute(route)
                },
                onClearLocalData = {
                    activeSessionStore.clear()
                    appSession = appSession.clear()
                    baselineProfile = TrailMateSampleData.baselineProfile
                    screen = TrailMateScreen.ONBOARDING
                }
            )
        }
    }
}

@Composable
private fun rememberTrailMateSessionStore(): TrailMateSessionStore {
    val context = LocalContext.current

    return remember(context) {
        SharedPreferencesTrailMateSessionStore(context)
    }
}

@Suppress("UNCHECKED_CAST")
private val BaselineProfileStateSaver = mapSaver(
    save = { profile ->
        mapOf(
            "exerciseFrequency" to profile.exerciseFrequency.name,
            "typicalDuration" to profile.typicalDuration.name,
            "experienceLevel" to profile.experienceLevel.name,
            "ascentExperience" to profile.ascentExperience.name,
            "heightCm" to (profile.heightCm ?: -1),
            "weightKg" to (profile.weightKg ?: -1),
            "commonPackWeightKg" to (profile.commonPackWeightKg ?: -1)
        )
    },
    restore = { saved ->
        BaselineProfile(
            exerciseFrequency = ExerciseFrequency.valueOf(saved["exerciseFrequency"] as String),
            typicalDuration = TypicalDuration.valueOf(saved["typicalDuration"] as String),
            experienceLevel = ExperienceLevel.valueOf(saved["experienceLevel"] as String),
            ascentExperience = AscentExperience.valueOf(saved["ascentExperience"] as String),
            heightCm = (saved["heightCm"] as Int).takeIf { it >= 0 },
            weightKg = (saved["weightKg"] as Int).takeIf { it >= 0 },
            commonPackWeightKg = (saved["commonPackWeightKg"] as Int).takeIf { it >= 0 }
        )
    }
)
