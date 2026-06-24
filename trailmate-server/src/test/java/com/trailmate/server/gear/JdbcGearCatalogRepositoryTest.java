package com.trailmate.server.gear;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdbcGearCatalogRepositoryTest {
    private JdbcTemplate jdbcTemplate;
    private JdbcGearCatalogRepository repository;

    @BeforeEach
    void setUp() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:trailmate_gear;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        jdbcTemplate = new JdbcTemplate(dataSource);
        createSchema();
        seedCatalog();
        repository = new JdbcGearCatalogRepository(jdbcTemplate);
    }

    @Test
    void searchReturnsBrandGearWithHostedImageUrl() {
        List<GearCatalogItem> results = repository.search("雨衣（防水透气）", "beta");

        assertEquals(1, results.size());
        GearCatalogItem item = results.get(0);
        assertEquals("cat_rain_arcteryx_beta_lt", item.catalogItemId());
        assertEquals("Arc'teryx", item.brand());
        assertEquals("/gear-thumbnails/arcteryx-beta-lt.png", item.imageUrl());
        assertEquals("TrailMate hosted catalog thumbnail", item.imageAttribution());
        assertTrue(item.tags().contains("防水"));
    }

    @Test
    void listCategoriesComesFromDatabase() {
        List<String> categories = repository.listCategories();

        assertEquals(List.of("头灯", "雨衣（防水透气）"), categories);
    }

    private void createSchema() {
        jdbcTemplate.execute("drop table if exists gear_catalog_item");
        jdbcTemplate.execute("""
            create table gear_catalog_item (
                catalog_item_id varchar(120) primary key,
                category varchar(120) not null,
                brand varchar(120) not null,
                model varchar(160) not null,
                display_name varchar(240) not null,
                weight_grams integer,
                tags_csv varchar(500) not null,
                image_url varchar(500),
                image_attribution varchar(240),
                source varchar(40) not null,
                active boolean not null
            )
            """);
    }

    private void seedCatalog() {
        jdbcTemplate.update(
            """
                insert into gear_catalog_item (
                    catalog_item_id, category, brand, model, display_name,
                    weight_grams, tags_csv, image_url, image_attribution, source, active
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
            "cat_rain_arcteryx_beta_lt",
            "雨衣（防水透气）",
            "Arc'teryx",
            "Beta LT Jacket",
            "Arc'teryx Beta LT Jacket",
            395,
            "防水,硬壳,暴露路段",
            "/gear-thumbnails/arcteryx-beta-lt.png",
            "TrailMate hosted catalog thumbnail",
            "seed",
            true
        );
        jdbcTemplate.update(
            """
                insert into gear_catalog_item (
                    catalog_item_id, category, brand, model, display_name,
                    weight_grams, tags_csv, image_url, image_attribution, source, active
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
            "cat_headlamp_bd_spot_400",
            "头灯",
            "Black Diamond",
            "Spot 400",
            "Black Diamond Spot 400",
            78,
            "夜间,备用电池,安全",
            "/gear-thumbnails/black-diamond-spot-400.png",
            "TrailMate hosted catalog thumbnail",
            "seed",
            true
        );
    }
}
