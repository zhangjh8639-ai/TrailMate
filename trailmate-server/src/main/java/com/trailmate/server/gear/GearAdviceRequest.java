package com.trailmate.server.gear;

import java.util.List;

public record GearAdviceRequest(
    String assessmentFingerprint,
    List<GearAdviceRecommendation> fallbackRecommendations,
    List<String> guardrails
) { }
