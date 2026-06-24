package com.trailmate.server.gear;

import java.util.List;

public record GearCatalogItem(
    String catalogItemId,
    String category,
    String brand,
    String model,
    String displayName,
    Integer weightGrams,
    List<String> tags,
    String imageUrl,
    String imageAttribution,
    String source
) { }
