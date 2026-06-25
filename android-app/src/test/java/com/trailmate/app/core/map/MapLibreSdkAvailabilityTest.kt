package com.trailmate.app.core.map

import org.junit.Assert.assertTrue
import org.junit.Test

class MapLibreSdkAvailabilityTest {
    @Test
    fun reportsLinkedWhenMapLibreMapViewClassIsOnClasspath() {
        assertTrue(MapLibreSdkAvailability.isLinked)
    }

    @Test
    fun classAvailabilityCheckDoesNotInitializeTargetClass() {
        assertTrue(
            MapLibreSdkAvailability.isClassAvailable(
                className = ExplodingStaticInitializer::class.java.name,
                classLoader = ExplodingStaticInitializer::class.java.classLoader
            )
        )
    }

    class ExplodingStaticInitializer {
        companion object {
            init {
                error("Availability probing should not initialize this class.")
            }
        }
    }
}
