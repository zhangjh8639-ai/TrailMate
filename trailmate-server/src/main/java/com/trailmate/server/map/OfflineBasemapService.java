package com.trailmate.server.map;

import java.util.List;

public class OfflineBasemapService {
    private final OfflineBasemapCatalogRepository catalogRepository;

    public OfflineBasemapService(OfflineBasemapCatalogRepository catalogRepository) {
        this.catalogRepository = catalogRepository;
    }

    public List<OfflineBasemapCatalogItem> listPmTilesCatalog(
        double minLongitude,
        double minLatitude,
        double maxLongitude,
        double maxLatitude
    ) {
        OfflineBasemapBounds routeBounds = new OfflineBasemapBounds(
            minLongitude,
            minLatitude,
            maxLongitude,
            maxLatitude
        );
        return catalogRepository.listPmTilesPacks().stream()
            .filter(pack -> pack.intersects(routeBounds))
            .toList();
    }
}
