package com.trailmate.app.services.tracking

import com.trailmate.app.core.database.TrackingRecordingStore
import com.trailmate.app.core.model.NavigationSession
import java.time.Instant

data class TrackingRecordingContext(
    val session: NavigationSession,
    val store: TrackingRecordingStore,
    val clock: () -> Instant = { Instant.now() },
)
