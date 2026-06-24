package com.trailmate.server.user;

public class UserProfileNotFoundException extends RuntimeException {
    public UserProfileNotFoundException() {
        super("User profile not found.");
    }
}
