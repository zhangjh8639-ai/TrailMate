package com.trailmate.server.map;

import java.util.List;

public class InMemoryOfflineBasemapCatalogRepository implements OfflineBasemapCatalogRepository {
    private static final List<OfflineBasemapCatalogItem> PMTILES_PACKS = List.of(
        new OfflineBasemapCatalogItem(
            "pmtiles_hangzhou_westlake_osm_v1",
            "杭州市 · 西湖区",
            "/offline-basemaps/pmtiles/hangzhou-westlake.pmtiles",
            120_000_000L,
            null,
            "MVT",
            10,
            14,
            120.00,
            30.05,
            120.30,
            30.40,
            "OpenStreetMap contributors",
            "OSM / Protomaps"
        )
    );

    @Override
    public List<OfflineBasemapCatalogItem> listPmTilesPacks() {
        return PMTILES_PACKS;
    }
}
