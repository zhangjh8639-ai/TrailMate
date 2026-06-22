package com.trailmate.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.trailmate.app.core.gpx.GpxImportQueuePolicy
import com.trailmate.app.core.location.TrackRecordingForegroundService
import com.trailmate.app.core.persistence.LocalTrailMateSessionRepository
import com.trailmate.app.core.persistence.SharedPreferencesTrailMateSessionStore
import com.trailmate.app.core.persistence.TrailMateSessionRepository
import com.trailmate.app.feature.home.HomeScreen
import com.trailmate.app.feature.onboarding.OnboardingScreen

enum class TrailMateScreen {
    ONBOARDING,
    HOME
}

@Composable
fun TrailMateApp(
    sessionRepository: TrailMateSessionRepository? = null,
    requestOnboardingLocationPermission: Boolean = false
) {
    val context = LocalContext.current
    val defaultSessionRepository = rememberTrailMateSessionRepository()
    val activeSessionRepository = sessionRepository ?: defaultSessionRepository
    var appSession by remember(activeSessionRepository) {
        val loadedSession = TrailMateAppSession(activeSessionRepository.loadSnapshot())
        val recoveredSession = loadedSession.recoverInterruptedGpxImports(
            nowEpochMillis = System.currentTimeMillis(),
            runningTimeoutMillis = GpxImportQueuePolicy.STARTUP_RUNNING_TIMEOUT_MILLIS,
            retryDelayMillis = GpxImportQueuePolicy.RETRY_DELAY_MILLIS
        )
        if (recoveredSession.snapshot.gpxImportQueue != loadedSession.snapshot.gpxImportQueue) {
            activeSessionRepository.saveGpxImportQueue(recoveredSession.snapshot.gpxImportQueue)
        }
        mutableStateOf(recoveredSession)
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
    DisposableEffect(context, activeSessionRepository) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                TrackRecordingForegroundService.trackRecordingFrom(intent)?.let { trackRecording ->
                    appSession = appSession.withTrackRecording(trackRecording)
                    activeSessionRepository.saveTrackRecording(trackRecording)
                }
            }
        }
        val filter = IntentFilter(TrackRecordingForegroundService.ACTION_RECORDING_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (screen) {
            TrailMateScreen.ONBOARDING -> OnboardingScreen(
                requestForegroundLocationPermissionOnComplete = requestOnboardingLocationPermission,
                onComplete = { profile, amapPrivacyConsent ->
                    appSession = appSession
                        .withProfile(profile)
                        .withAmapPrivacyConsent(amapPrivacyConsent)
                    baselineProfile = profile
                    activeSessionRepository.saveProfile(profile)
                    activeSessionRepository.saveAmapPrivacyConsent(amapPrivacyConsent)
                    screen = TrailMateScreen.HOME
                }
            )
            TrailMateScreen.HOME -> HomeScreen(
                profile = baselineProfile,
                initialInventory = appSession.snapshot.inventory,
                initialImportedRoute = appSession.snapshot.importedRoute,
                initialHistoricalActivities = appSession.snapshot.historicalActivities,
                initialGpxImportQueue = appSession.snapshot.gpxImportQueue,
                initialTrackRecording = appSession.snapshot.latestTrackRecording,
                initialAmapPrivacyConsent = appSession.snapshot.amapPrivacyConsent,
                initialOfflineRoutePackKeys = appSession.snapshot.savedOfflineRoutePackKeys,
                initialOfflineBaseMapTileProofs = appSession.snapshot.offlineBaseMapTileProofs,
                onInventoryChanged = { inventory ->
                    appSession = appSession.withInventory(inventory)
                    activeSessionRepository.saveInventory(inventory)
                },
                onRouteImported = { route ->
                    appSession = appSession.withImportedRoute(route)
                    activeSessionRepository.saveImportedRoute(route)
                },
                onHistoricalActivitiesChanged = { historicalActivities ->
                    appSession = appSession.withHistoricalActivities(historicalActivities)
                    activeSessionRepository.saveHistoricalActivities(historicalActivities)
                },
                onGpxImportQueueChanged = { queue ->
                    appSession = appSession.withGpxImportQueue(queue)
                    activeSessionRepository.saveGpxImportQueue(queue)
                },
                onTrackRecordingChanged = { trackRecording ->
                    appSession = appSession.withTrackRecording(trackRecording)
                    activeSessionRepository.saveTrackRecording(trackRecording)
                },
                onOfflineRoutePackKeysChanged = { keys ->
                    appSession = appSession.withOfflineRoutePackKeys(keys)
                    activeSessionRepository.saveOfflineRoutePackKeys(keys)
                },
                onOfflineBaseMapTileProofsChanged = { proofs ->
                    appSession = appSession.withOfflineBaseMapTileProofs(proofs)
                    activeSessionRepository.saveOfflineBaseMapTileProofs(proofs)
                },
                onClearLocalData = {
                    activeSessionRepository.clearLocalData()
                    appSession = appSession.clear()
                    baselineProfile = TrailMateSampleData.baselineProfile
                    screen = TrailMateScreen.ONBOARDING
                }
            )
        }
    }
}

@Composable
private fun rememberTrailMateSessionRepository(): TrailMateSessionRepository {
    val context = LocalContext.current

    return remember(context) {
        LocalTrailMateSessionRepository(SharedPreferencesTrailMateSessionStore(context))
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
