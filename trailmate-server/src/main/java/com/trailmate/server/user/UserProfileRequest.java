package com.trailmate.server.user;

public record UserProfileRequest(
    String exerciseFrequency,
    String typicalDuration,
    String experienceLevel,
    String ascentExperience,
    Integer heightCm,
    Integer weightKg,
    Integer commonPackWeightKg
) { }
