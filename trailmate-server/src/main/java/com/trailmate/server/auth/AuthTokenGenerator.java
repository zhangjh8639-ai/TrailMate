package com.trailmate.server.auth;

public interface AuthTokenGenerator {
    String nextToken(String prefix);
}
