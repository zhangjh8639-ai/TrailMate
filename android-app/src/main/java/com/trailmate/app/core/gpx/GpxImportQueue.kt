package com.trailmate.app.core.gpx

enum class GpxImportJobKind { TARGET_ROUTE, HISTORICAL_ACTIVITY }
enum class GpxImportJobStatus { QUEUED, RUNNING, WAITING_RETRY, SUCCEEDED, FAILED }

data class GpxImportJob(
    val id: String,
    val kind: GpxImportJobKind,
    val sourceUri: String,
    val fileName: String,
    val status: GpxImportJobStatus,
    val attemptCount: Int,
    val maxAttempts: Int,
    val nextAttemptAtEpochMillis: Long?,
    val lastError: String?,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long
)

data class GpxImportQueue(
    val jobs: List<GpxImportJob> = emptyList()
) {
    fun enqueue(
        id: String,
        kind: GpxImportJobKind,
        sourceUri: String,
        fileName: String,
        nowEpochMillis: Long,
        maxAttempts: Int = DEFAULT_MAX_ATTEMPTS
    ): GpxImportQueue {
        require(id.isNotBlank()) { "Import job id is required." }
        require(sourceUri.isNotBlank()) { "Import source URI is required." }
        require(fileName.isNotBlank()) { "Import file name is required." }
        require(maxAttempts > 0) { "Import max attempts must be positive." }

        if (jobs.any { job -> job.id == id }) {
            return this
        }

        return copy(
            jobs = jobs + GpxImportJob(
                id = id,
                kind = kind,
                sourceUri = sourceUri,
                fileName = fileName,
                status = GpxImportJobStatus.QUEUED,
                attemptCount = 0,
                maxAttempts = maxAttempts,
                nextAttemptAtEpochMillis = null,
                lastError = null,
                createdAtEpochMillis = nowEpochMillis,
                updatedAtEpochMillis = nowEpochMillis
            )
        )
    }

    fun nextRunnableJob(nowEpochMillis: Long): GpxImportJob? =
        jobs.firstOrNull { job ->
            job.status == GpxImportJobStatus.QUEUED ||
                (job.status == GpxImportJobStatus.WAITING_RETRY &&
                    job.nextAttemptAtEpochMillis != null &&
                    job.nextAttemptAtEpochMillis <= nowEpochMillis)
        }

    fun hasRunningJob(): Boolean =
        jobs.any { job -> job.status == GpxImportJobStatus.RUNNING }

    fun startNext(nowEpochMillis: Long): GpxImportQueue {
        if (hasRunningJob()) {
            return this
        }

        val nextJob = nextRunnableJob(nowEpochMillis) ?: return this

        return updateJob(nextJob.id) { job ->
            job.copy(
                status = GpxImportJobStatus.RUNNING,
                attemptCount = job.attemptCount + 1,
                nextAttemptAtEpochMillis = null,
                lastError = null,
                updatedAtEpochMillis = nowEpochMillis
            )
        }
    }

    fun startJob(id: String, nowEpochMillis: Long): GpxImportQueue =
        updateExistingJob(id) { job ->
            if (hasRunningJob()) {
                return@updateExistingJob job
            }
            if (!job.isRunnable(nowEpochMillis)) {
                return@updateExistingJob job
            }
            job.copy(
                status = GpxImportJobStatus.RUNNING,
                attemptCount = job.attemptCount + 1,
                nextAttemptAtEpochMillis = null,
                lastError = null,
                updatedAtEpochMillis = nowEpochMillis
            )
        }

    fun markSucceeded(id: String, nowEpochMillis: Long): GpxImportQueue =
        updateExistingJob(id) { job ->
            if (job.status != GpxImportJobStatus.RUNNING) {
                return@updateExistingJob job
            }
            job.copy(
                status = GpxImportJobStatus.SUCCEEDED,
                nextAttemptAtEpochMillis = null,
                lastError = null,
                updatedAtEpochMillis = nowEpochMillis
            )
        }

    fun markFailed(
        id: String,
        message: String,
        nowEpochMillis: Long,
        retryDelayMillis: Long
    ): GpxImportQueue =
        updateExistingJob(id) { job ->
            if (job.status != GpxImportJobStatus.RUNNING) {
                return@updateExistingJob job
            }
            val canRetry = job.attemptCount < job.maxAttempts
            job.copy(
                status = if (canRetry) GpxImportJobStatus.WAITING_RETRY else GpxImportJobStatus.FAILED,
                nextAttemptAtEpochMillis = if (canRetry) nowEpochMillis + retryDelayMillis.coerceAtLeast(0L) else null,
                lastError = message,
                updatedAtEpochMillis = nowEpochMillis
            )
        }

    fun recoverInterruptedRunningJobs(
        nowEpochMillis: Long,
        runningTimeoutMillis: Long,
        retryDelayMillis: Long
    ): GpxImportQueue =
        copy(
            jobs = jobs.map { job ->
                if (job.status != GpxImportJobStatus.RUNNING ||
                    job.updatedAtEpochMillis + runningTimeoutMillis > nowEpochMillis
                ) {
                    return@map job
                }

                val canRetry = job.attemptCount < job.maxAttempts
                job.copy(
                    status = if (canRetry) GpxImportJobStatus.WAITING_RETRY else GpxImportJobStatus.FAILED,
                    nextAttemptAtEpochMillis = if (canRetry) nowEpochMillis + retryDelayMillis.coerceAtLeast(0L) else null,
                    lastError = INTERRUPTED_ERROR,
                    updatedAtEpochMillis = nowEpochMillis
                )
            }
        )

    private fun updateJob(id: String, transform: (GpxImportJob) -> GpxImportJob): GpxImportQueue =
        copy(
            jobs = jobs.map { job ->
                if (job.id == id) transform(job) else job
            }
        )

    private fun updateExistingJob(id: String, transform: (GpxImportJob) -> GpxImportJob): GpxImportQueue {
        require(jobs.any { job -> job.id == id }) { "Import job $id was not found." }
        return updateJob(id = id, transform = transform)
    }

    companion object {
        const val DEFAULT_MAX_ATTEMPTS = 3
        const val INTERRUPTED_ERROR = "Import interrupted before completion."
    }
}

private fun GpxImportJob.isRunnable(nowEpochMillis: Long): Boolean =
    status == GpxImportJobStatus.QUEUED ||
        (status == GpxImportJobStatus.WAITING_RETRY &&
            nextAttemptAtEpochMillis != null &&
            nextAttemptAtEpochMillis <= nowEpochMillis)
