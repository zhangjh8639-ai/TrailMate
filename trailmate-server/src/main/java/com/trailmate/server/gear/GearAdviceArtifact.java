package com.trailmate.server.gear;

import java.time.Instant;
import java.util.List;

public record GearAdviceArtifact(
    String artifactId,
    String userId,
    String planId,
    String assessmentFingerprint,
    List<GearAdviceRecommendation> recommendations,
    Instant createdAt
) { }
