package com.trailmate.app

import com.trailmate.app.core.gpx.GpxImportQueue
import com.trailmate.app.core.model.BaselineProfile
import com.trailmate.app.core.model.GearInventory
import com.trailmate.app.core.model.HistoricalActivity
import com.trailmate.app.core.model.ImportedRoute
import com.trailmate.app.core.model.TrailMateSampleData
import com.trailmate.app.core.persistence.TrailMateSnapshot

data class TrailMateAppSession(
    val snapshot: TrailMateSnapshot
) {
    val hasProfile: Boolean
        get() = snapshot.profile != null

    val baselineProfile: BaselineProfile
        get() = snapshot.profile ?: TrailMateSampleData.baselineProfile

    fun withProfile(profile: BaselineProfile): TrailMateAppSession =
        copy(snapshot = snapshot.copy(profile = profile))

    fun withInventory(inventory: GearInventory): TrailMateAppSession =
        copy(snapshot = snapshot.copy(inventory = inventory))

    fun withImportedRoute(route: ImportedRoute): TrailMateAppSession =
        copy(snapshot = snapshot.copy(importedRoute = route))

    fun withHistoricalActivities(historicalActivities: List<HistoricalActivity>): TrailMateAppSession =
        copy(snapshot = snapshot.copy(historicalActivities = historicalActivities))

    fun withGpxImportQueue(queue: GpxImportQueue): TrailMateAppSession =
        copy(snapshot = snapshot.copy(gpxImportQueue = queue))

    fun recoverInterruptedGpxImports(
        nowEpochMillis: Long,
        runningTimeoutMillis: Long,
        retryDelayMillis: Long
    ): TrailMateAppSession =
        withGpxImportQueue(
            snapshot.gpxImportQueue.recoverInterruptedRunningJobs(
                nowEpochMillis = nowEpochMillis,
                runningTimeoutMillis = runningTimeoutMillis,
                retryDelayMillis = retryDelayMillis
            )
        )

    fun clear(): TrailMateAppSession =
        TrailMateAppSession(TrailMateSnapshot.empty())
}
