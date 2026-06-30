package com.trailmate.server.gear;

public class GearAdviceAuthRequiredException extends RuntimeException {
    public GearAdviceAuthRequiredException() {
        super("Login is required before requesting AI gear advice.");
    }
}
