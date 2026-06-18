package com.trailmate.app.core.persistence

data class CloudDataControlContext(
    val accountState: CloudAccountState,
    val snapshot: TrailMateSnapshot,
    val syncState: CloudDataControlSyncState = CloudDataControlSyncState.Resolved
)

enum class CloudAccountState {
    SignedIn,
    LocalOnly
}

enum class CloudDataControlSyncState {
    Resolved,
    PendingOrConflicted
}

enum class CloudDataControlAvailability {
    Available,
    Blocked
}

enum class CloudDataControlDataSet {
    Profile,
    Gear,
    GearChecklistArtifact,
    AuditRecord,
    ImportedTargetRoute,
    HistoricalGpxActivity,
    GpxImportQueueArtifact
}

enum class CloudDataControlDeletionTarget {
    CloudProfile,
    CloudGear,
    CloudGearChecklistArtifact,
    LocalProfileCache,
    LocalGearCache,
    AuditTombstoneConfirmation,
    RouteLibrary,
    HistoricalGpxActivity,
    GpxImportQueueArtifact
}

data class CloudDataControlAuditRecord(
    val operation: String,
    val detail: String
)

data class CloudProfileGearExportPackage(
    val availability: CloudDataControlAvailability,
    val includedData: Set<CloudDataControlDataSet>,
    val excludedData: Set<CloudDataControlDataSet>,
    val auditRecord: CloudDataControlAuditRecord?,
    val message: String,
    val warning: String? = null,
    val blockedReason: String? = null
)

data class CloudProfileGearDeletionPlan(
    val availability: CloudDataControlAvailability,
    val targets: Set<CloudDataControlDeletionTarget>,
    val excludedTargets: Set<CloudDataControlDeletionTarget>,
    val auditRecord: CloudDataControlAuditRecord?,
    val message: String,
    val blockedReason: String? = null
)

object CloudDataControlPolicy {
    private val profileGearExportData = setOf(
        CloudDataControlDataSet.Profile,
        CloudDataControlDataSet.Gear,
        CloudDataControlDataSet.GearChecklistArtifact,
        CloudDataControlDataSet.AuditRecord
    )

    private val profileGearExportExclusions = setOf(
        CloudDataControlDataSet.ImportedTargetRoute,
        CloudDataControlDataSet.HistoricalGpxActivity,
        CloudDataControlDataSet.GpxImportQueueArtifact
    )

    private val profileGearDeletionTargets = setOf(
        CloudDataControlDeletionTarget.CloudProfile,
        CloudDataControlDeletionTarget.CloudGear,
        CloudDataControlDeletionTarget.CloudGearChecklistArtifact,
        CloudDataControlDeletionTarget.LocalProfileCache,
        CloudDataControlDeletionTarget.LocalGearCache,
        CloudDataControlDeletionTarget.AuditTombstoneConfirmation
    )

    private val profileGearDeletionExclusions = setOf(
        CloudDataControlDeletionTarget.RouteLibrary,
        CloudDataControlDeletionTarget.HistoricalGpxActivity,
        CloudDataControlDeletionTarget.GpxImportQueueArtifact
    )

    fun exportProfileAndGear(context: CloudDataControlContext): CloudProfileGearExportPackage {
        if (context.accountState == CloudAccountState.LocalOnly) {
            return CloudProfileGearExportPackage(
                availability = CloudDataControlAvailability.Blocked,
                includedData = emptySet(),
                excludedData = profileGearExportExclusions,
                auditRecord = null,
                message = "Cloud profile and gear export is unavailable.",
                blockedReason = "Please sign in to export cloud profile and gear data."
            )
        }

        return CloudProfileGearExportPackage(
            availability = CloudDataControlAvailability.Available,
            includedData = profileGearExportData,
            excludedData = profileGearExportExclusions,
            auditRecord = CloudDataControlAuditRecord(
                operation = "profile_gear_export",
                detail = "Prepared export snapshot for profile and ${context.snapshot.inventory.items.size} gear items."
            ),
            message = "Your profile and gear export package is ready.",
            warning = if (context.syncState == CloudDataControlSyncState.PendingOrConflicted) {
                "Pending sync or conflicts may make this export snapshot stale."
            } else {
                null
            }
        )
    }

    fun planProfileAndGearDeletion(context: CloudDataControlContext): CloudProfileGearDeletionPlan {
        if (context.accountState == CloudAccountState.LocalOnly) {
            return CloudProfileGearDeletionPlan(
                availability = CloudDataControlAvailability.Blocked,
                targets = emptySet(),
                excludedTargets = profileGearDeletionExclusions,
                auditRecord = null,
                message = "Cloud profile and gear deletion is unavailable.",
                blockedReason = "Please sign in to delete cloud profile and gear data."
            )
        }

        if (context.syncState == CloudDataControlSyncState.PendingOrConflicted) {
            return CloudProfileGearDeletionPlan(
                availability = CloudDataControlAvailability.Blocked,
                targets = emptySet(),
                excludedTargets = profileGearDeletionExclusions,
                auditRecord = null,
                message = "Cloud profile and gear deletion is blocked.",
                blockedReason = "Resolve pending sync or conflicts before deleting cloud profile and gear data."
            )
        }

        return CloudProfileGearDeletionPlan(
            availability = CloudDataControlAvailability.Available,
            targets = profileGearDeletionTargets,
            excludedTargets = profileGearDeletionExclusions,
            auditRecord = CloudDataControlAuditRecord(
                operation = "profile_gear_delete",
                detail = "Prepared deletion tombstone for profile and ${context.snapshot.inventory.items.size} gear items."
            ),
            message = "Cloud profile and gear deletion plan is ready."
        )
    }
}
