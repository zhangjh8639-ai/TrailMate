package com.trailmate.server.map;

import java.util.List;

public interface OfflineBasemapCatalogRepository {
    List<OfflineBasemapCatalogItem> listPmTilesPacks();
}
