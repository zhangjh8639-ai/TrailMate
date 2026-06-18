package com.trailmate.app.core.persistence

import com.trailmate.app.core.gpx.GpxImportQueue
import com.trailmate.app.core.gpx.GpxImportJobKind
import com.trailmate.app.core.model.BaselineProfile
import com.trailmate.app.core.model.GearInventory
import com.trailmate.app.core.model.HistoricalActivity
import com.trailmate.app.core.model.ImportedRoute
import com.trailmate.app.core.model.TrailMateSampleData
import org.junit.Assert.assertEquals
import org.junit.Test

class LocalTrailMateSessionRepositoryTest {
    @Test
    fun loadsSnapshotFromLocalStore() {
        val snapshot = TrailMateSnapshot(
            profile = TrailMateSampleData.baselineProfile,
            historicalActivities = TrailMateSampleData.historicalActivities
        )
        val repository = LocalTrailMateSessionRepository(FakeTrailMateSessionStore(snapshot))

        assertEquals(snapshot, repository.loadSnapshot())
    }

    @Test
    fun savesProfileAndHistoryThroughLocalStore() {
        val store = FakeTrailMateSessionStore()
        val repository = LocalTrailMateSessionRepository(store)
        val inventory = GearInventory(TrailMateSampleData.gearItems)

        repository.saveProfile(TrailMateSampleData.baselineProfile)
        repository.saveInventory(inventory)
        repository.saveImportedRoute(TrailMateSampleData.importedTargetRoute)
        repository.saveHistoricalActivities(TrailMateSampleData.historicalActivities)
        val queue = GpxImportQueue().enqueue(
            id = "job-1",
            kind = GpxImportJobKind.TARGET_ROUTE,
            sourceUri = "content://trailmate/routes/ridge",
            fileName = "ridge.gpx",
            nowEpochMillis = 1_000L
        )
        repository.saveGpxImportQueue(queue)

        assertEquals(TrailMateSampleData.baselineProfile, store.snapshot.profile)
        assertEquals(inventory, store.snapshot.inventory)
        assertEquals(TrailMateSampleData.importedTargetRoute, store.snapshot.importedRoute)
        assertEquals(TrailMateSampleData.historicalActivities, store.snapshot.historicalActivities)
        assertEquals(queue, store.snapshot.gpxImportQueue)
    }

    @Test
    fun clearLocalDataWritesExplicitEmptySnapshot() {
        val store = FakeTrailMateSessionStore(
            TrailMateSnapshot(
                profile = TrailMateSampleData.baselineProfile,
                inventory = GearInventory(TrailMateSampleData.gearItems),
                importedRoute = TrailMateSampleData.importedTargetRoute,
                historicalActivities = TrailMateSampleData.historicalActivities
            )
        )
        val repository = LocalTrailMateSessionRepository(store)

        repository.clearLocalData()

        assertEquals(TrailMateSnapshot.empty(), store.snapshot)
    }
}

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

    override fun saveHistoricalActivities(historicalActivities: List<HistoricalActivity>) {
        snapshot = snapshot.copy(historicalActivities = historicalActivities)
    }

    override fun saveGpxImportQueue(queue: GpxImportQueue) {
        snapshot = snapshot.copy(gpxImportQueue = queue)
    }

    override fun clear() {
        snapshot = TrailMateSnapshot.empty()
    }
}
