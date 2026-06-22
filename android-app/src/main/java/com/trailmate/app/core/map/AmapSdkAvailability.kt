package com.trailmate.app.core.map

object AmapSdkAvailability {
    val isLinked: Boolean
        get() = runCatching {
            Class.forName("com.amap.api.maps.MapView")
        }.isSuccess
}
