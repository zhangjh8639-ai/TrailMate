package com.trailmate.app

import android.Manifest
import android.content.Context
import android.location.LocationManager
import androidx.test.platform.app.InstrumentationRegistry
import com.trailmate.app.core.location.TrackRecordingForegroundService
import com.trailmate.app.core.model.TrackRecordingState
import com.trailmate.app.core.model.TrackRecordingStatus
import com.trailmate.app.core.persistence.SharedPreferencesTrailMateSessionStore
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TrackRecordingForegroundServiceSmokeTest {
    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun prepare() {
        clearSession()
        grantRuntimePermission(Manifest.permission.ACCESS_FINE_LOCATION)
        grantRuntimePermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        grantRuntimePermission(Manifest.permission.POST_NOTIFICATIONS)
        setSystemLocationEnabled(true)
    }

    @After
    fun cleanUp() {
        runCatching { TrackRecordingForegroundService.finishRecording(context) }
        setSystemLocationEnabled(true)
        clearSession()
    }

    @Test
    fun serviceStartsAndFinishesForegroundTrackRecordingWhenGpsIsReady() {
        TrackRecordingForegroundService.startRecording(context, "龙井山脊")

        waitForRecordingStatus(TrackRecordingStatus.RECORDING)
        assertEquals("龙井山脊", currentRecordingRouteName())

        TrackRecordingForegroundService.finishRecording(context)

        waitForRecordingStatus(TrackRecordingStatus.FINISHED)
    }

    @Test
    fun serviceResumePausesActiveRecordingWhenGpsProviderIsDisabled() {
        SharedPreferencesTrailMateSessionStore(context).saveTrackRecording(
            TrackRecordingState(
                status = TrackRecordingStatus.RECORDING,
                routeName = "龙井山脊",
                startedAtEpochMillis = System.currentTimeMillis() - 60_000L,
                recordingActiveSinceEpochMillis = System.currentTimeMillis() - 60_000L
            )
        )

        setSystemLocationEnabled(false)
        TrackRecordingForegroundService.resumeRecording(context)

        waitForRecordingStatus(TrackRecordingStatus.PAUSED)
        assertEquals("龙井山脊", currentRecordingRouteName())
    }

    private fun waitForRecordingStatus(status: TrackRecordingStatus) {
        val deadline = System.currentTimeMillis() + STATUS_TIMEOUT_MS
        while (System.currentTimeMillis() < deadline) {
            if (currentRecordingStatus() == status) {
                return
            }
            Thread.sleep(POLL_INTERVAL_MS)
        }
        assertEquals(status, currentRecordingStatus())
    }

    private fun currentRecordingStatus(): TrackRecordingStatus =
        SharedPreferencesTrailMateSessionStore(context)
            .load()
            .latestTrackRecording
            .status

    private fun currentRecordingRouteName(): String? =
        SharedPreferencesTrailMateSessionStore(context)
            .load()
            .latestTrackRecording
            .routeName

    private fun grantRuntimePermission(permission: String) {
        shell("pm grant ${context.packageName} $permission")
    }

    private fun setSystemLocationEnabled(enabled: Boolean) {
        if (enabled) {
            shell("cmd location set-location-enabled true")
            shell("cmd location providers add-test-provider ${LocationManager.GPS_PROVIDER}")
            shell("cmd location providers set-test-provider-enabled ${LocationManager.GPS_PROVIDER} true")
        } else {
            shell("cmd location providers set-test-provider-enabled ${LocationManager.GPS_PROVIDER} false")
            shell("cmd location providers remove-test-provider ${LocationManager.GPS_PROVIDER}")
            shell("cmd location set-location-enabled false")
        }
    }

    private fun shell(command: String) {
        InstrumentationRegistry.getInstrumentation()
            .uiAutomation
            .executeShellCommand(command)
            .use { }
    }

    private fun clearSession() {
        context.getSharedPreferences("trailmate_session", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    private companion object {
        const val STATUS_TIMEOUT_MS = 5_000L
        const val POLL_INTERVAL_MS = 100L
    }
}
