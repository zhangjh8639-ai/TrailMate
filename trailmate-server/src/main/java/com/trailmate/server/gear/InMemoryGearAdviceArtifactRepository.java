package com.trailmate.server.gear;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class InMemoryGearAdviceArtifactRepository implements GearAdviceArtifactRepository {
    private final List<GearAdviceArtifact> artifacts = new ArrayList<>();

    @Override
    public synchronized void save(GearAdviceArtifact artifact) {
        artifacts.add(artifact);
    }

    @Override
    public synchronized Optional<GearAdviceArtifact> latestFor(
        String userId,
        String planId,
        String assessmentFingerprint
    ) {
        return artifacts.stream()
            .filter(artifact -> artifact.userId().equals(userId))
            .filter(artifact -> artifact.planId().equals(planId))
            .filter(artifact -> artifact.assessmentFingerprint().equals(assessmentFingerprint))
            .max(Comparator.comparing(GearAdviceArtifact::createdAt));
    }
}
