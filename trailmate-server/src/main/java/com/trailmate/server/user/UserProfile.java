package com.trailmate.server.user;

import java.time.Instant;

public record UserProfile(
    String userId,
    String exerciseFrequency,
    String typicalDuration,
    String experienceLevel,
    String ascentExperience,
    Integer heightCm,
    Integer weightKg,
    Integer commonPackWeightKg,
    Instant updatedAt
) { }
