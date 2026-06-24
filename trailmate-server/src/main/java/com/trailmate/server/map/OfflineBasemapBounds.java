package com.trailmate.server.map;

public record OfflineBasemapBounds(
    double minLongitude,
    double minLatitude,
    double maxLongitude,
    double maxLatitude
) {
    public OfflineBasemapBounds {
        if (minLongitude < -180.0 || minLongitude > 180.0
            || maxLongitude < -180.0 || maxLongitude > 180.0
            || minLatitude < -90.0 || minLatitude > 90.0
            || maxLatitude < -90.0 || maxLatitude > 90.0
            || minLongitude > maxLongitude
            || minLatitude > maxLatitude) {
            throw new IllegalArgumentException("Invalid offline basemap bounds.");
        }
    }

    public boolean intersects(OfflineBasemapBounds other) {
        return minLongitude <= other.maxLongitude()
            && maxLongitude >= other.minLongitude()
            && minLatitude <= other.maxLatitude()
            && maxLatitude >= other.minLatitude();
    }
}
