package com.trailmate.app

import com.trailmate.app.core.gpx.GpxImportJobKind
import com.trailmate.app.core.gpx.GpxImportJobStatus
import com.trailmate.app.core.gpx.GpxImportQueue
import com.trailmate.app.core.persistence.TrailMateSnapshot
import org.junit.Assert.assertEquals
import org.junit.Test

class TrailMateAppSessionTest {
    @Test
    fun recoverInterruptedGpxImportsConvertsStaleRunningJobsOnStartup() {
        val queue = GpxImportQueue()
            .enqueue(
                id = "job-1",
                kind = GpxImportJobKind.TARGET_ROUTE,
                sourceUri = "content://trailmate/routes/ridge",
                fileName = "ridge.gpx",
                nowEpochMillis = 1_000L
            )
            .startNext(nowEpochMillis = 1_100L)
        val session = TrailMateAppSession(TrailMateSnapshot(gpxImportQueue = queue))

        val recovered = session.recoverInterruptedGpxImports(
            nowEpochMillis = 121_100L,
            runningTimeoutMillis = 120_000L,
            retryDelayMillis = 30_000L
        )

        val recoveredJob = recovered.snapshot.gpxImportQueue.jobs.single()
        assertEquals(GpxImportJobStatus.WAITING_RETRY, recoveredJob.status)
        assertEquals(151_100L, recoveredJob.nextAttemptAtEpochMillis)
    }

    @Test
    fun recoverInterruptedGpxImportsCanTreatAnyRestoredRunningJobAsInterrupted() {
        val queue = GpxImportQueue()
            .enqueue(
                id = "job-1",
                kind = GpxImportJobKind.TARGET_ROUTE,
                sourceUri = "content://trailmate/routes/ridge",
                fileName = "ridge.gpx",
                nowEpochMillis = 1_000L
            )
            .startNext(nowEpochMillis = 1_100L)
        val session = TrailMateAppSession(TrailMateSnapshot(gpxImportQueue = queue))

        val recovered = session.recoverInterruptedGpxImports(
            nowEpochMillis = 1_101L,
            runningTimeoutMillis = 0L,
            retryDelayMillis = 30_000L
        )

        assertEquals(GpxImportJobStatus.WAITING_RETRY, recovered.snapshot.gpxImportQueue.jobs.single().status)
    }
}
