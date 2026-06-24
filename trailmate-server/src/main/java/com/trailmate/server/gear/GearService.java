package com.trailmate.server.gear;

import java.util.List;

public class GearService {
    private final GearCatalogRepository catalogRepository;

    public GearService(GearCatalogRepository catalogRepository) {
        this.catalogRepository = catalogRepository;
    }

    public List<String> listCategories() {
        return catalogRepository.listCategories();
    }

    public List<GearCatalogItem> searchCatalog(String category, String query) {
        return catalogRepository.search(category, query);
    }
}
