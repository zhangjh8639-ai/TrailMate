package com.trailmate.server.gear;

import java.util.List;
import java.util.Optional;

public interface GearCatalogRepository {
    List<String> listCategories();

    List<GearCatalogItem> search(String category, String query);

    Optional<GearCatalogItem> findById(String catalogItemId);
}
