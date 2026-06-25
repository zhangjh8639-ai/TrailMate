package com.trailmate.server.gear;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class JdbcGearCatalogRepository implements GearCatalogRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcGearCatalogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<String> listCategories() {
        return jdbcTemplate.queryForList(
            """
                select distinct category
                from gear_catalog_item
                where active = true
                order by category
                """,
            String.class
        );
    }

    @Override
    public List<GearCatalogItem> search(String category, String query) {
        String normalizedCategory = normalize(category);
        String normalizedQuery = normalize(query);
        String likeQuery = "%" + normalizedQuery + "%";

        return jdbcTemplate.query(
            """
                select catalog_item_id, category, brand, model, display_name,
                       weight_grams, tags_csv, image_url, image_attribution, source
                from gear_catalog_item
                where active = true
                  and (? = '' or lower(category) = ?)
                  and (
                    ? = ''
                    or lower(category || ' ' || brand || ' ' || model || ' ' || display_name || ' ' || tags_csv) like ?
                  )
                order by display_name
                """,
            (rs, rowNum) -> mapItem(rs),
            normalizedCategory,
            normalizedCategory,
            normalizedQuery,
            likeQuery
        );
    }

    @Override
    public Optional<GearCatalogItem> findById(String catalogItemId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                """
                    select catalog_item_id, category, brand, model, display_name,
                           weight_grams, tags_csv, image_url, image_attribution, source
                    from gear_catalog_item
                    where catalog_item_id = ?
                      and active = true
                    """,
                (rs, rowNum) -> mapItem(rs),
                catalogItemId
            ));
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    private GearCatalogItem mapItem(ResultSet rs) throws SQLException {
        return new GearCatalogItem(
            rs.getString("catalog_item_id"),
            rs.getString("category"),
            rs.getString("brand"),
            rs.getString("model"),
            rs.getString("display_name"),
            nullableInt(rs, "weight_grams"),
            splitTags(rs.getString("tags_csv")),
            rs.getString("image_url"),
            rs.getString("image_attribution"),
            rs.getString("source")
        );
    }

    private Integer nullableInt(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }

    private List<String> splitTags(String tagsCsv) {
        if (tagsCsv == null || tagsCsv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(tagsCsv.split(","))
            .map(String::trim)
            .filter(value -> !value.isBlank())
            .toList();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
