package com.trailmate.app.core.persistence

import com.trailmate.app.core.gpx.GpxImportJobKind
import com.trailmate.app.core.gpx.GpxImportQueue
import com.trailmate.app.core.model.GearInventory
import com.trailmate.app.core.model.TrailMateSampleData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CloudDataControlPolicyTest {
    @Test
    fun signedInProfileGearExportIncludesOnlyProfileGearAuditAndMessage() {
        val snapshot = TrailMateSnapshot(
            profile = TrailMateSampleData.baselineProfile,
            inventory = GearInventory(TrailMateSampleData.gearItems),
            importedRoute = TrailMateSampleData.importedTargetRoute,
            historicalActivities = TrailMateSampleData.historicalActivities,
            gpxImportQueue = GpxImportQueue().enqueue(
                id = "job-target-1",
                kind = GpxImportJobKind.TARGET_ROUTE,
                sourceUri = "content://trailmate/routes/1",
                fileName = "ridge.gpx",
                nowEpochMillis = 1_000L
            )
        )

        val exportPackage = CloudDataControlPolicy.exportProfileAndGear(
            CloudDataControlContext(
                accountState = CloudAccountState.SignedIn,
                snapshot = snapshot
            )
        )

        assertEquals(CloudDataControlAvailability.Available, exportPackage.availability)
        assertEquals(
            setOf(
                CloudDataControlDataSet.Profile,
                CloudDataControlDataSet.Gear,
                CloudDataControlDataSet.GearChecklistArtifact,
                CloudDataControlDataSet.AuditRecord
            ),
            exportPackage.includedData
        )
        assertTrue(exportPackage.excludedData.contains(CloudDataControlDataSet.ImportedTargetRoute))
        assertTrue(exportPackage.excludedData.contains(CloudDataControlDataSet.HistoricalGpxActivity))
        assertTrue(exportPackage.excludedData.contains(CloudDataControlDataSet.GpxImportQueueArtifact))
        assertNotNull(exportPackage.auditRecord)
        assertTrue(exportPackage.message.contains("profile and gear"))
        assertFalse(exportPackage.message.contains("route"))
        assertFalse(exportPackage.message.contains("GPX"))
    }

    @Test
    fun signedInProfileGearDeletionPlanDoesNotDeleteRoutesOrHistoricalGpx() {
        val deletionPlan = CloudDataControlPolicy.planProfileAndGearDeletion(
            CloudDataControlContext(
                accountState = CloudAccountState.SignedIn,
                snapshot = TrailMateSnapshot(
                    profile = TrailMateSampleData.baselineProfile,
                    inventory = GearInventory(TrailMateSampleData.gearItems),
                    importedRoute = TrailMateSampleData.importedTargetRoute,
                    historicalActivities = TrailMateSampleData.historicalActivities,
                    gpxImportQueue = GpxImportQueue().enqueue(
                        id = "job-history-1",
                        kind = GpxImportJobKind.HISTORICAL_ACTIVITY,
                        sourceUri = "content://trailmate/history/1",
                        fileName = "history.gpx",
                        nowEpochMillis = 1_000L
                    )
                )
            )
        )

        assertEquals(CloudDataControlAvailability.Available, deletionPlan.availability)
        assertEquals(
            setOf(
                CloudDataControlDeletionTarget.CloudProfile,
                CloudDataControlDeletionTarget.CloudGear,
                CloudDataControlDeletionTarget.CloudGearChecklistArtifact,
                CloudDataControlDeletionTarget.LocalProfileCache,
                CloudDataControlDeletionTarget.LocalGearCache,
                CloudDataControlDeletionTarget.AuditTombstoneConfirmation
            ),
            deletionPlan.targets
        )
        assertTrue(deletionPlan.excludedTargets.contains(CloudDataControlDeletionTarget.RouteLibrary))
        assertTrue(deletionPlan.excludedTargets.contains(CloudDataControlDeletionTarget.HistoricalGpxActivity))
        assertTrue(deletionPlan.excludedTargets.contains(CloudDataControlDeletionTarget.GpxImportQueueArtifact))
        assertNotNull(deletionPlan.auditRecord)
    }

    @Test
    fun localOnlyStateBlocksCloudExportAndDeletionWithSignInReason() {
        val context = CloudDataControlContext(
            accountState = CloudAccountState.LocalOnly,
            snapshot = TrailMateSnapshot.empty()
        )

        val exportPackage = CloudDataControlPolicy.exportProfileAndGear(context)
        val deletionPlan = CloudDataControlPolicy.planProfileAndGearDeletion(context)

        assertEquals(CloudDataControlAvailability.Blocked, exportPackage.availability)
        assertTrue(exportPackage.includedData.isEmpty())
        assertTrue(exportPackage.blockedReason.orEmpty().contains("sign in"))

        assertEquals(CloudDataControlAvailability.Blocked, deletionPlan.availability)
        assertTrue(deletionPlan.targets.isEmpty())
        assertTrue(deletionPlan.blockedReason.orEmpty().contains("sign in"))
    }

    @Test
    fun pendingSyncBlocksDeletionButAllowsExportSnapshotWithStaleWarning() {
        val context = CloudDataControlContext(
            accountState = CloudAccountState.SignedIn,
            snapshot = TrailMateSnapshot(
                profile = TrailMateSampleData.baselineProfile,
                inventory = GearInventory(TrailMateSampleData.gearItems)
            ),
            syncState = CloudDataControlSyncState.PendingOrConflicted
        )

        val exportPackage = CloudDataControlPolicy.exportProfileAndGear(context)
        val deletionPlan = CloudDataControlPolicy.planProfileAndGearDeletion(context)

        assertEquals(CloudDataControlAvailability.Available, exportPackage.availability)
        assertTrue(exportPackage.warning.orEmpty().contains("stale"))
        assertEquals(
            setOf(
                CloudDataControlDataSet.Profile,
                CloudDataControlDataSet.Gear,
                CloudDataControlDataSet.GearChecklistArtifact,
                CloudDataControlDataSet.AuditRecord
            ),
            exportPackage.includedData
        )

        assertEquals(CloudDataControlAvailability.Blocked, deletionPlan.availability)
        assertTrue(deletionPlan.targets.isEmpty())
        assertTrue(deletionPlan.blockedReason.orEmpty().contains("sync"))
    }
}
