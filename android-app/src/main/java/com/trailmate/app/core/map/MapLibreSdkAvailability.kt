package com.trailmate.app.core.map

object MapLibreSdkAvailability {
    val isLinked: Boolean
        get() = isClassAvailable(
            className = "org.maplibre.android.maps.MapView",
            classLoader = MapLibreSdkAvailability::class.java.classLoader
        )

    internal fun isClassAvailable(className: String, classLoader: ClassLoader?): Boolean =
        runCatching {
            Class.forName(className, false, classLoader)
        }.isSuccess
}
