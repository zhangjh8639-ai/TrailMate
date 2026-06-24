package com.trailmate.server.map;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OfflineBasemapControllerTest {
    private final OfflineBasemapService service = new OfflineBasemapService(
        new InMemoryOfflineBasemapCatalogRepository()
    );
    private final MockMvc mockMvc = MockMvcBuilders
        .standaloneSetup(new OfflineBasemapController(service))
        .build();

    @Test
    void pmTilesCatalogEndpointReturnsPacksCoveringRequestedBounds() throws Exception {
        mockMvc.perform(get("/api/v1/offline-basemaps/pmtiles/catalog")
                .param("minLongitude", "120.05")
                .param("minLatitude", "30.10")
                .param("maxLongitude", "120.25")
                .param("maxLatitude", "30.35"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].packId").value("pmtiles_hangzhou_westlake_osm_v1"))
            .andExpect(jsonPath("$[0].regionName").value("杭州市 · 西湖区"))
            .andExpect(jsonPath("$[0].downloadUrl").value("/offline-basemaps/pmtiles/hangzhou-westlake.pmtiles"))
            .andExpect(jsonPath("$[0].tileType").value("MVT"))
            .andExpect(jsonPath("$[0].source").value("OSM / Protomaps"));
    }

    @Test
    void pmTilesCatalogEndpointReturnsEmptyListWhenNoPackCoversBounds() throws Exception {
        mockMvc.perform(get("/api/v1/offline-basemaps/pmtiles/catalog")
                .param("minLongitude", "116.30")
                .param("minLatitude", "39.80")
                .param("maxLongitude", "116.50")
                .param("maxLatitude", "40.00"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void pmTilesCatalogEndpointRejectsInvalidBounds() throws Exception {
        mockMvc.perform(get("/api/v1/offline-basemaps/pmtiles/catalog")
                .param("minLongitude", "120.25")
                .param("minLatitude", "30.10")
                .param("maxLongitude", "120.05")
                .param("maxLatitude", "30.35"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("OFFLINE_BASEMAP_INVALID_BOUNDS"));
    }
}
