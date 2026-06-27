package com.trailmate.server.map;

public record OfflineBasemapCatalogItem(
    String packId,
    String regionName,
    String downloadUrl,
    Long sizeBytes,
    String sha256,
    String tileType,
    int minZoom,
    int maxZoom,
    double minLongitude,
    double minLatitude,
    double maxLongitude,
    double maxLatitude,
    String attribution,
    String source
) {
    public boolean intersects(OfflineBasemapBounds routeBounds) {
        return bounds().intersects(routeBounds);
    }

    public boolean contains(OfflineBasemapBounds routeBounds) {
        return bounds().contains(routeBounds);
    }

    private OfflineBasemapBounds bounds() {
        return new OfflineBasemapBounds(
            minLongitude,
            minLatitude,
            maxLongitude,
            maxLatitude
        );
    }
}
