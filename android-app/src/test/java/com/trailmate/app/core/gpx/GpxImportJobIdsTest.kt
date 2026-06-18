package com.trailmate.app.core.gpx

import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GpxImportJobIdsTest {
    @Test
    fun createUsesNonceSoManualReimportCanCreateANewJobForTheSameSource() {
        val first = GpxImportJobIds.create(
            kind = GpxImportJobKind.TARGET_ROUTE,
            nonce = 1_000L
        )
        val second = GpxImportJobIds.create(
            kind = GpxImportJobKind.TARGET_ROUTE,
            nonce = 1_001L
        )

        assertNotEquals(first, second)
        assertTrue(first.startsWith("target_route:"))
        assertTrue(second.startsWith("target_route:"))
    }

    @Test
    fun createAcceptsNegativeSystemNonceValues() {
        val id = GpxImportJobIds.create(
            kind = GpxImportJobKind.HISTORICAL_ACTIVITY,
            nonce = -1L
        )

        assertTrue(id.startsWith("historical_activity:"))
    }
}
