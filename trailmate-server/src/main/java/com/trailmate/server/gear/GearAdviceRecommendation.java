package com.trailmate.server.gear;

public record GearAdviceRecommendation(
    String category,
    GearAdviceStatus status,
    String rationale,
    String matchedGearItemId
) { }
