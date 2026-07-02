package com.trailmate.app.core.location

import com.trailmate.app.core.geo.LocationSample

class RecordingLocationObserver : LocationProviderObserver {
    val samples = mutableListOf<LocationSample>()
    val statuses = mutableListOf<LocationProviderStatus>()

    override fun onLocationSample(sample: LocationSample) {
        samples += sample
    }

    override fun onProviderStatus(status: LocationProviderStatus) {
        statuses += status
    }
}
