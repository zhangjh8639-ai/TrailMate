package com.trailmate.server.gear;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GearServiceTest {
    private final GearService service = new GearService(new InMemoryGearCatalogRepository());

    @Test
    void listCategoriesReturnsStableOutdoorCategories() {
        List<String> categories = service.listCategories();

        assertTrue(categories.contains("雨衣（防水透气）"));
        assertTrue(categories.contains("头灯"));
        assertTrue(categories.contains("登山杖"));
    }

    @Test
    void catalogCoversCoreRouteChecklistCategoriesWithBrandItems() {
        List<String> requiredCategories = List.of(
            "雨衣（防水透气）",
            "头灯",
            "登山杖",
            "保温层（抓绒/羽绒）",
            "备用水",
            "徒步鞋",
            "急救包",
            "移动电源",
            "导航设备",
            "背包"
        );

        for (String category : requiredCategories) {
            List<GearCatalogItem> items = service.searchCatalog(category, "");

            assertFalse(items.isEmpty(), category + " should have at least one selectable catalog item.");
            assertTrue(
                items.stream().allMatch(item -> !item.brand().isBlank() && !item.model().isBlank()),
                category + " catalog items must be brand/model based."
            );
        }
    }

    @Test
    void searchFindsCatalogItemsByCategoryAndQuery() {
        List<GearCatalogItem> results = service.searchCatalog("雨衣（防水透气）", "beta");

        assertFalse(results.isEmpty());
        GearCatalogItem item = results.get(0);
        assertEquals("cat_rain_arcteryx_beta_lt", item.catalogItemId());
        assertEquals("Arc'teryx", item.brand());
        assertEquals("Beta LT Jacket", item.model());
        assertEquals("雨衣（防水透气）", item.category());
    }

    @Test
    void searchFindsHikingShoesWithHostedImageUrl() {
        List<GearCatalogItem> results = service.searchCatalog("徒步鞋", "salomon");

        GearCatalogItem item = results.get(0);
        assertEquals("cat_shoes_salomon_x_ultra_4_gtx", item.catalogItemId());
        assertEquals("Salomon", item.brand());
        assertEquals("X Ultra 4 GTX", item.model());
        assertEquals("/gear-thumbnails/salomon-x-ultra-4-gtx.png", item.imageUrl());
    }

    @Test
    void catalogImageUrlsResolveToPackagedStaticThumbnails() throws Exception {
        byte[] pngSignature = new byte[] {
            (byte) 0x89, 0x50, 0x4E, 0x47,
            0x0D, 0x0A, 0x1A, 0x0A
        };

        for (GearCatalogItem item : service.searchCatalog("", "")) {
            assertTrue(
                item.imageUrl().startsWith("/gear-thumbnails/"),
                item.catalogItemId() + " should expose a server-hosted thumbnail path."
            );
            try (InputStream input = GearServiceTest.class.getResourceAsStream("/static" + item.imageUrl())) {
                assertNotNull(input, item.catalogItemId() + " thumbnail must be packaged as a static resource.");
                assertArrayEquals(pngSignature, input.readNBytes(pngSignature.length));
            }
        }
    }

    @Test
    void serviceDoesNotExposePersonalInventoryMethods() {
        List<String> methodNames = Arrays.stream(GearService.class.getDeclaredMethods())
            .map(java.lang.reflect.Method::getName)
            .toList();

        assertFalse(methodNames.contains("listInventory"));
        assertFalse(methodNames.contains("saveInventoryItem"));
        assertFalse(methodNames.contains("updateInventoryItem"));
        assertFalse(methodNames.contains("deleteInventoryItem"));
    }

}
