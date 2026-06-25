package com.trailmate.app.core.gpx

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class GpxImportQueueTest {
    @Test
    fun failedJobWaitsUntilRetryTimeBeforeRestarting() {
        val queue = GpxImportQueue()
            .enqueue(
                id = "job-1",
                kind = GpxImportJobKind.TARGET_ROUTE,
                sourceUri = "content://trailmate/routes/ridge",
                fileName = "ridge.gpx",
                nowEpochMillis = 1_000L
            )
            .startNext(nowEpochMillis = 1_100L)
            .markFailed(
                id = "job-1",
                message = "Parser unavailable.",
                nowEpochMillis = 1_200L,
                retryDelayMillis = 60_000L
            )

        val waitingJob = queue.jobs.single()
        assertEquals(GpxImportJobStatus.WAITING_RETRY, waitingJob.status)
        assertEquals(1, waitingJob.attemptCount)
        assertEquals(61_200L, waitingJob.nextAttemptAtEpochMillis)
        assertNull(queue.nextRunnableJob(nowEpochMillis = 61_199L))

        val restarted = queue.startNext(nowEpochMillis = 61_200L).jobs.single()

        assertEquals(GpxImportJobStatus.RUNNING, restarted.status)
        assertEquals(2, restarted.attemptCount)
        assertNull(restarted.lastError)
        assertNull(restarted.nextAttemptAtEpochMillis)
    }

    @Test
    fun exhaustedRetryBudgetFailsWithoutAutomaticRestart() {
        val queue = GpxImportQueue()
            .enqueue(
                id = "job-1",
                kind = GpxImportJobKind.HISTORICAL_ACTIVITY,
                sourceUri = "content://trailmate/history/old-ridge",
                fileName = "old-ridge.gpx",
                nowEpochMillis = 1_000L,
                maxAttempts = 1
            )
            .startNext(nowEpochMillis = 1_100L)
            .markFailed(
                id = "job-1",
                message = "No GPX points.",
                nowEpochMillis = 1_200L,
                retryDelayMillis = 60_000L
            )

        assertEquals(GpxImportJobStatus.FAILED, queue.jobs.single().status)
        assertNull(queue.jobs.single().nextAttemptAtEpochMillis)
        assertNull(queue.nextRunnableJob(nowEpochMillis = 120_000L))
        assertEquals(queue, queue.startNext(nowEpochMillis = 120_000L))
    }

    @Test
    fun successfulJobClearsRetryMetadata() {
        val queue = GpxImportQueue()
            .enqueue(
                id = "job-1",
                kind = GpxImportJobKind.TARGET_ROUTE,
                sourceUri = "content://trailmate/routes/ridge",
                fileName = "ridge.gpx",
                nowEpochMillis = 1_000L
            )
            .startNext(nowEpochMillis = 1_100L)
            .markFailed(
                id = "job-1",
                message = "Temporary resolver error.",
                nowEpochMillis = 1_200L,
                retryDelayMillis = 1L
            )
            .startNext(nowEpochMillis = 1_201L)
            .markSucceeded(id = "job-1", nowEpochMillis = 1_300L)

        val job = queue.jobs.single()
        assertEquals(GpxImportJobStatus.SUCCEEDED, job.status)
        assertEquals(2, job.attemptCount)
        assertNull(job.lastError)
        assertNull(job.nextAttemptAtEpochMillis)
    }

    @Test
    fun startNextDoesNotStartAnotherJobWhileOneIsRunning() {
        val queue = GpxImportQueue()
            .enqueue(
                id = "job-1",
                kind = GpxImportJobKind.TARGET_ROUTE,
                sourceUri = "content://trailmate/routes/ridge",
                fileName = "ridge.gpx",
                nowEpochMillis = 1_000L
            )
            .enqueue(
                id = "job-2",
                kind = GpxImportJobKind.HISTORICAL_ACTIVITY,
                sourceUri = "content://trailmate/history/old-ridge",
                fileName = "old-ridge.gpx",
                nowEpochMillis = 1_000L
            )
            .startNext(nowEpochMillis = 1_100L)

        val unchanged = queue.startNext(nowEpochMillis = 1_200L)

        assertEquals(queue, unchanged)
        assertEquals(GpxImportJobStatus.RUNNING, unchanged.jobs[0].status)
        assertEquals(GpxImportJobStatus.QUEUED, unchanged.jobs[1].status)
    }

    @Test
    fun hasRunningJobDetectsActiveImport() {
        val queue = GpxImportQueue()
            .enqueue(
                id = "job-1",
                kind = GpxImportJobKind.TARGET_ROUTE,
                sourceUri = "content://trailmate/routes/ridge",
                fileName = "ridge.gpx",
                nowEpochMillis = 1_000L
            )
            .startNext(nowEpochMillis = 1_100L)

        assertEquals(true, queue.hasRunningJob())
    }

    @Test
    fun startJobMarksOnlyTheRequestedRunnableJobRunning() {
        val queue = GpxImportQueue()
            .enqueue(
                id = "job-1",
                kind = GpxImportJobKind.TARGET_ROUTE,
                sourceUri = "content://trailmate/routes/ridge",
                fileName = "ridge.gpx",
                nowEpochMillis = 1_000L
            )
            .enqueue(
                id = "job-2",
                kind = GpxImportJobKind.HISTORICAL_ACTIVITY,
                sourceUri = "content://trailmate/history/old-ridge",
                fileName = "old-ridge.gpx",
                nowEpochMillis = 1_000L
            )

        val started = queue.startJob(id = "job-2", nowEpochMillis = 1_100L)

        assertEquals(GpxImportJobStatus.QUEUED, started.jobs[0].status)
        assertEquals(GpxImportJobStatus.RUNNING, started.jobs[1].status)
    }

    @Test
    fun startJobDoesNotStartRequestedJobWhileAnotherJobIsRunning() {
        val queue = GpxImportQueue()
            .enqueue(
                id = "job-1",
                kind = GpxImportJobKind.TARGET_ROUTE,
                sourceUri = "content://trailmate/routes/ridge",
                fileName = "ridge.gpx",
                nowEpochMillis = 1_000L
            )
            .enqueue(
                id = "job-2",
                kind = GpxImportJobKind.HISTORICAL_ACTIVITY,
                sourceUri = "content://trailmate/history/old-ridge",
                fileName = "old-ridge.gpx",
                nowEpochMillis = 1_000L
            )
            .startJob(id = "job-1", nowEpochMillis = 1_100L)

        val unchanged = queue.startJob(id = "job-2", nowEpochMillis = 1_200L)

        assertEquals(queue, unchanged)
    }

    @Test
    fun staleRunningJobRecoversToRetryInsteadOfBlockingQueueForever() {
        val queue = GpxImportQueue()
            .enqueue(
                id = "job-1",
                kind = GpxImportJobKind.TARGET_ROUTE,
                sourceUri = "content://trailmate/routes/ridge",
                fileName = "ridge.gpx",
                nowEpochMillis = 1_000L
            )
            .startNext(nowEpochMillis = 1_100L)
            .recoverInterruptedRunningJobs(
                nowEpochMillis = 121_100L,
                runningTimeoutMillis = 120_000L,
                retryDelayMillis = 30_000L
            )

        val job = queue.jobs.single()
        assertEquals(GpxImportJobStatus.WAITING_RETRY, job.status)
        assertEquals(1, job.attemptCount)
        assertEquals(151_100L, job.nextAttemptAtEpochMillis)
        assertEquals("Import interrupted before completion.", job.lastError)
    }

    @Test
    fun staleRunningJobExhaustsRetryBudgetWhenLastAttemptWasInterrupted() {
        val queue = GpxImportQueue()
            .enqueue(
                id = "job-1",
                kind = GpxImportJobKind.TARGET_ROUTE,
                sourceUri = "content://trailmate/routes/ridge",
                fileName = "ridge.gpx",
                nowEpochMillis = 1_000L,
                maxAttempts = 1
            )
            .startNext(nowEpochMillis = 1_100L)
            .recoverInterruptedRunningJobs(
                nowEpochMillis = 121_100L,
                runningTimeoutMillis = 120_000L,
                retryDelayMillis = 30_000L
            )

        val job = queue.jobs.single()
        assertEquals(GpxImportJobStatus.FAILED, job.status)
        assertNull(job.nextAttemptAtEpochMillis)
        assertEquals("Import interrupted before completion.", job.lastError)
    }

    @Test
    fun staleCallbacksCannotMoveSucceededJobBackToFailed() {
        val succeeded = GpxImportQueue()
            .enqueue(
                id = "job-1",
                kind = GpxImportJobKind.TARGET_ROUTE,
                sourceUri = "content://trailmate/routes/ridge",
                fileName = "ridge.gpx",
                nowEpochMillis = 1_000L
            )
            .startNext(nowEpochMillis = 1_100L)
            .markSucceeded(id = "job-1", nowEpochMillis = 1_200L)

        val afterStaleFailure = succeeded.markFailed(
            id = "job-1",
            message = "Late failure callback.",
            nowEpochMillis = 1_300L,
            retryDelayMillis = 30_000L
        )

        assertEquals(succeeded, afterStaleFailure)
    }

    @Test
    fun completionForUnknownJobFailsFast() {
        val queue = GpxImportQueue()

        assertThrows(IllegalArgumentException::class.java) {
            queue.markSucceeded(id = "missing-job", nowEpochMillis = 1_000L)
        }
    }
}
