package com.trailmate.app.core.gpx

object GpxImportQueuePolicy {
    const val RETRY_DELAY_MILLIS = 60_000L
    const val RUNNING_TIMEOUT_MILLIS = 120_000L
    const val STARTUP_RUNNING_TIMEOUT_MILLIS = 0L
}
