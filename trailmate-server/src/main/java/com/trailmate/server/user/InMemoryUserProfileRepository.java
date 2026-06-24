package com.trailmate.server.user;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryUserProfileRepository implements UserProfileRepository {
    private final ConcurrentHashMap<String, UserProfile> profilesByUserId = new ConcurrentHashMap<>();

    @Override
    public UserProfile save(UserProfile profile) {
        profilesByUserId.put(profile.userId(), profile);
        return profile;
    }

    @Override
    public Optional<UserProfile> findByUserId(String userId) {
        return Optional.ofNullable(profilesByUserId.get(userId));
    }
}
