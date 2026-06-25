package com.trailmate.server.user;

import java.time.Clock;
import java.util.List;

public class UserProfileService {
    private static final List<String> EXERCISE_FREQUENCIES = List.of(
        "RARELY",
        "ONE_TO_TWO_PER_WEEK",
        "THREE_PLUS_PER_WEEK"
    );
    private static final List<String> TYPICAL_DURATIONS = List.of(
        "UNDER_30",
        "MIN_30_TO_60",
        "OVER_60"
    );
    private static final List<String> EXPERIENCE_LEVELS = List.of(
        "BEGINNER",
        "REGULAR",
        "EXPERIENCED"
    );
    private static final List<String> ASCENT_EXPERIENCES = List.of(
        "UNDER_300",
        "M300_TO_800",
        "OVER_800"
    );

    private final UserProfileRepository repository;
    private final Clock clock;

    public UserProfileService(UserProfileRepository repository) {
        this(repository, Clock.systemUTC());
    }

    public UserProfileService(UserProfileRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public UserProfile saveProfile(String userId, UserProfileRequest request) {
        String normalizedUserId = requireUserId(userId);
        requireAllowed("exerciseFrequency", request.exerciseFrequency(), EXERCISE_FREQUENCIES);
        requireAllowed("typicalDuration", request.typicalDuration(), TYPICAL_DURATIONS);
        requireAllowed("experienceLevel", request.experienceLevel(), EXPERIENCE_LEVELS);
        requireAllowed("ascentExperience", request.ascentExperience(), ASCENT_EXPERIENCES);
        requireRange("heightCm", request.heightCm(), 80, 230);
        requireRange("weightKg", request.weightKg(), 25, 250);
        requireRange("commonPackWeightKg", request.commonPackWeightKg(), 0, 60);

        return repository.save(new UserProfile(
            normalizedUserId,
            request.exerciseFrequency(),
            request.typicalDuration(),
            request.experienceLevel(),
            request.ascentExperience(),
            request.heightCm(),
            request.weightKg(),
            request.commonPackWeightKg(),
            clock.instant()
        ));
    }

    public UserProfile getProfile(String userId) {
        String normalizedUserId = requireUserId(userId);
        return repository.findByUserId(normalizedUserId)
            .orElseThrow(UserProfileNotFoundException::new);
    }

    private String requireUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User id is required.");
        }
        return userId.trim();
    }

    private void requireAllowed(String field, String value, List<String> allowedValues) {
        if (value == null || !allowedValues.contains(value)) {
            throw new IllegalArgumentException("Invalid " + field + ".");
        }
    }

    private void requireRange(String field, Integer value, int min, int max) {
        if (value != null && (value < min || value > max)) {
            throw new IllegalArgumentException("Invalid " + field + ".");
        }
    }
}
