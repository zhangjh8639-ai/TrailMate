package com.trailmate.server.gear;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GearControllerTest {
    private final GearService service = new GearService(new InMemoryGearCatalogRepository());
    private final MockMvc mockMvc = MockMvcBuilders
        .standaloneSetup(new GearController(service))
        .build();

    @Test
    void categoriesEndpointReturnsCatalogCategories() throws Exception {
        mockMvc.perform(get("/api/v1/gear/catalog/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0]").exists());
    }

    @Test
    void catalogSearchEndpointReturnsMatchingItems() throws Exception {
        mockMvc.perform(get("/api/v1/gear/catalog/search")
                .param("category", "雨衣（防水透气）")
                .param("q", "beta"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].catalogItemId").value("cat_rain_arcteryx_beta_lt"))
            .andExpect(jsonPath("$[0].brand").value("Arc'teryx"))
            .andExpect(jsonPath("$[0].imageUrl").value("/gear-thumbnails/arcteryx-beta-lt.png"));
    }

    @Test
    void catalogSearchAcceptsQueryAlias() throws Exception {
        mockMvc.perform(get("/api/v1/gear/catalog/search")
                .param("category", "雨衣（防水透气）")
                .param("query", "beta"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].catalogItemId").value("cat_rain_arcteryx_beta_lt"));
    }

    @Test
    void inventoryEndpointsAreNotPublicApi() throws Exception {
        mockMvc.perform(get("/api/v1/gear/inventory"))
            .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/v1/gear/inventory"))
            .andExpect(status().isNotFound());
        mockMvc.perform(patch("/api/v1/gear/inventory/gear_123"))
            .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/v1/gear/inventory/gear_123"))
            .andExpect(status().isNotFound());
    }
}
