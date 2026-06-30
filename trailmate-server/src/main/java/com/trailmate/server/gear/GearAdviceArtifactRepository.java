package com.trailmate.server.gear;

import java.util.Optional;

public interface GearAdviceArtifactRepository {
    void save(GearAdviceArtifact artifact);

    Optional<GearAdviceArtifact> latestFor(
        String userId,
        String planId,
        String assessmentFingerprint
    );
}
