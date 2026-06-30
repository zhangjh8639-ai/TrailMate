package com.trailmate.server.gear;

import java.util.List;

public record GearAdviceResponse(
    String assessmentFingerprint,
    List<GearAdviceRecommendation> recommendations
) { }
