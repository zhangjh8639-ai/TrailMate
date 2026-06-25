package com.trailmate.app.core.map

import org.junit.Assert.assertTrue
import org.junit.Test

class AmapSdkAvailabilityTest {
    @Test
    fun reportsLinkedWhenAmapMapViewClassIsOnClasspath() {
        assertTrue(AmapSdkAvailability.isLinked)
    }
}
