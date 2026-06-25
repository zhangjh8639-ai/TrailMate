package com.trailmate.server.gear;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GearSchemaMigrationTest {
    private static final Path MIGRATION = Path.of(
        "src/main/resources/db/migration/V2__create_gear_catalog_schema.sql"
    );
    private static final Path PREVIEW_USER_MIGRATION = Path.of(
        "src/main/resources/db/migration/V3__seed_preview_app_user.sql"
    );
    private static final Path CATALOG_ONLY_INVENTORY_MIGRATION = Path.of(
        "src/main/resources/db/migration/V5__enforce_catalog_only_gear_inventory.sql"
    );
    private static final Path HIKING_SHOE_CATALOG_MIGRATION = Path.of(
        "src/main/resources/db/migration/V6__seed_hiking_shoe_catalog_item.sql"
    );
    private static final Path CORE_ROUTE_CHECKLIST_CATALOG_MIGRATION = Path.of(
        "src/main/resources/db/migration/V7__seed_core_route_checklist_gear_catalog.sql"
    );
    private static final Path HOSTED_THUMBNAIL_MIGRATION = Path.of(
        "src/main/resources/db/migration/V8__host_gear_catalog_thumbnail_urls.sql"
    );
    private static final Path DROP_PERSONAL_INVENTORY_MIGRATION = Path.of(
        "src/main/resources/db/migration/V9__drop_personal_gear_inventory.sql"
    );
    private static final Path HOSTED_THUMBNAILS = Path.of(
        "src/main/resources/static/gear-thumbnails"
    );

    @Test
    void migrationCreatesServerOwnedGearCatalogTables() throws Exception {
        assertTrue(Files.exists(MIGRATION), "Gear catalog schema migration must exist.");
        String sql = Files.readString(MIGRATION);

        assertTrue(sql.contains("create table gear_catalog_item"));
        assertTrue(sql.contains("create table user_gear_inventory"));
        assertTrue(sql.contains("image_url text"));
        assertTrue(sql.contains("image_attribution text"));
        assertTrue(sql.contains("catalog_item_id text references gear_catalog_item(catalog_item_id)"));
        assertTrue(sql.contains("custom = false and catalog_item_id is not null"));
        assertTrue(sql.contains("create unique index uq_user_gear_inventory_catalog_active"));
    }

    @Test
    void migrationSeedsSelectableBrandGearWithImageUrls() throws Exception {
        assertTrue(Files.exists(MIGRATION), "Gear catalog schema migration must exist.");
        String sql = Files.readString(MIGRATION);

        assertTrue(sql.contains("cat_rain_arcteryx_beta_lt"));
        assertTrue(sql.contains("Arc''teryx"));
        assertTrue(sql.contains("Black Diamond"));
        assertTrue(sql.contains("https://"));
    }

    @Test
    void migrationSeedsPreviewUserForAuthenticatedCatalogData() throws Exception {
        assertTrue(Files.exists(PREVIEW_USER_MIGRATION), "Preview app user migration must exist.");
        String sql = Files.readString(PREVIEW_USER_MIGRATION);

        assertTrue(sql.contains("insert into app_user"));
        assertTrue(sql.contains("local-preview-user"));
        assertTrue(sql.contains("on conflict (id)"));
    }

    @Test
    void historicalMigrationEnforcesCatalogOnlyInventoryRows() throws Exception {
        assertTrue(
            Files.exists(CATALOG_ONLY_INVENTORY_MIGRATION),
            "Historical catalog-only inventory migration must remain available for existing databases."
        );
        String sql = Files.readString(CATALOG_ONLY_INVENTORY_MIGRATION);

        assertTrue(sql.contains("chk_user_gear_inventory_catalog_only"));
        assertTrue(sql.contains("custom = false"));
        assertTrue(sql.contains("catalog_item_id is not null"));
    }

    @Test
    void migrationAddsHikingShoeCatalogItemForExistingDatabases() throws Exception {
        assertTrue(
            Files.exists(HIKING_SHOE_CATALOG_MIGRATION),
            "Existing deployments need a new migration for hiking shoe catalog seed data."
        );
        String sql = Files.readString(HIKING_SHOE_CATALOG_MIGRATION);

        assertTrue(sql.contains("cat_shoes_salomon_x_ultra_4_gtx"));
        assertTrue(sql.contains("Salomon"));
        assertTrue(sql.contains("X Ultra 4 GTX"));
        assertTrue(sql.contains("image_url"));
        assertTrue(sql.contains("on conflict (catalog_item_id)"));
    }

    @Test
    void migrationAddsCoreRouteChecklistCatalogItemsForExistingDatabases() throws Exception {
        assertTrue(
            Files.exists(CORE_ROUTE_CHECKLIST_CATALOG_MIGRATION),
            "Existing deployments need catalog seed data for core route checklist items."
        );
        String sql = Files.readString(CORE_ROUTE_CHECKLIST_CATALOG_MIGRATION);

        assertTrue(sql.contains("cat_first_aid_adventure_medical_ultralight"));
        assertTrue(sql.contains("cat_power_nitecore_nb10000"));
        assertTrue(sql.contains("cat_navigation_garmin_etrex_se"));
        assertTrue(sql.contains("cat_pack_osprey_talon_22"));
        assertTrue(sql.contains("on conflict (catalog_item_id)"));
    }

    @Test
    void migrationMovesSeedCatalogToHostedThumbnails() throws Exception {
        assertTrue(
            Files.exists(HOSTED_THUMBNAIL_MIGRATION),
            "Existing deployments need catalog image URLs updated to hosted thumbnails."
        );
        String sql = Files.readString(HOSTED_THUMBNAIL_MIGRATION);

        assertTrue(sql.contains("cat_rain_arcteryx_beta_lt"));
        assertTrue(sql.contains("/gear-thumbnails/arcteryx-beta-lt.png"));
        assertTrue(sql.contains("TrailMate hosted catalog thumbnail"));
        assertTrue(sql.contains("updated_at = now()"));
    }

    @Test
    void migrationRemovesObsoletePersonalGearInventoryTable() throws Exception {
        assertTrue(
            Files.exists(DROP_PERSONAL_INVENTORY_MIGRATION),
            "Production catalog matching should drop the obsolete personal gear inventory table."
        );
        String sql = Files.readString(DROP_PERSONAL_INVENTORY_MIGRATION);

        assertTrue(sql.contains("drop table if exists user_gear_inventory"));
    }

    @Test
    void hostedThumbnailFilesExistForSeedCatalog() {
        assertTrue(Files.exists(HOSTED_THUMBNAILS.resolve("arcteryx-beta-lt.png")));
        assertTrue(Files.exists(HOSTED_THUMBNAILS.resolve("black-diamond-spot-400.png")));
        assertTrue(Files.exists(HOSTED_THUMBNAILS.resolve("salomon-x-ultra-4-gtx.png")));
        assertTrue(Files.exists(HOSTED_THUMBNAILS.resolve("leki-legacy-lite-as.png")));
        assertTrue(Files.exists(HOSTED_THUMBNAILS.resolve("montbell-plasma-1000.png")));
        assertTrue(Files.exists(HOSTED_THUMBNAILS.resolve("hydrapak-seeker-2l.png")));
        assertTrue(Files.exists(HOSTED_THUMBNAILS.resolve("adventure-medical-ultralight-7.png")));
        assertTrue(Files.exists(HOSTED_THUMBNAILS.resolve("nitecore-nb10000-gen2.png")));
        assertTrue(Files.exists(HOSTED_THUMBNAILS.resolve("garmin-etrex-se.png")));
        assertTrue(Files.exists(HOSTED_THUMBNAILS.resolve("osprey-talon-22.png")));
    }
}
