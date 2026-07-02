package com.trailmate.app.platform.location

import com.trailmate.app.core.location.LocationProviderObserver
import com.trailmate.app.core.location.LocationProviderStatus

internal class AndroidLocationProviderState(
    subscribedProviders: Set<String>,
) {
    private val activeProviders = subscribedProviders.toMutableSet()

    @Volatile
    var isStopped: Boolean = false
        private set

    fun shouldForwardLocation(): Boolean =
        !isStopped && activeProviders.isNotEmpty()

    fun stop() {
        isStopped = true
        activeProviders.clear()
    }

    fun markProviderDisabled(
        provider: String,
        observer: LocationProviderObserver,
    ) {
        if (isStopped) return

        activeProviders -= provider
        if (activeProviders.isEmpty()) {
            observer.onProviderStatus(LocationProviderStatus.Disabled)
        }
    }

    fun markProviderEnabled(
        provider: String,
        observer: LocationProviderObserver,
    ) {
        if (isStopped) return

        val wasDisabled = activeProviders.isEmpty()
        activeProviders += provider
        if (wasDisabled) {
            observer.onProviderStatus(LocationProviderStatus.Ready)
        }
    }
}
