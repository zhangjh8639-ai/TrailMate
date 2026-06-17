package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteImportRulesTest {
    @Test
    fun parsedTargetRouteCanBeAssessed() {
        val route = TrailMateSampleData.importedTargetRoute

        assertTrue(route.readyForAssessment())
        assertEquals("15.2 km / +860 m", route.summaryLabel())
    }
}
