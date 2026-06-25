package com.trailmate.server.user;

import java.util.Optional;

public interface UserProfileRepository {
    UserProfile save(UserProfile profile);

    Optional<UserProfile> findByUserId(String userId);
}
