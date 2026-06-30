package com.trailmate.server.gear;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class GearAdviceService {
    private final GearCatalogRepository catalogRepository;
    private final GearAdviceArtifactRepository artifactRepository;
    private final Clock clock;

    public GearAdviceService(
        GearCatalogRepository catalogRepository,
        GearAdviceArtifactRepository artifactRepository
    ) {
        this(catalogRepository, artifactRepository, Clock.systemUTC());
    }

    GearAdviceService(
        GearCatalogRepository catalogRepository,
        GearAdviceArtifactRepository artifactRepository,
        Clock clock
    ) {
        this.catalogRepository = catalogRepository;
        this.artifactRepository = artifactRepository;
        this.clock = clock;
    }

    public GearAdviceResponse advise(
        String userId,
        String planId,
        GearAdviceRequest request
    ) {
        validateRequest(userId, planId, request);
        String normalizedUserId = userId.trim();
        String normalizedPlanId = planId.trim();
        String normalizedFingerprint = request.assessmentFingerprint().trim();
        var existingArtifact = artifactRepository.latestFor(
            normalizedUserId,
            normalizedPlanId,
            normalizedFingerprint
        );
        if (existingArtifact.isPresent()) {
            return new GearAdviceResponse(
                existingArtifact.get().assessmentFingerprint(),
                existingArtifact.get().recommendations()
            );
        }

        List<GearAdviceRecommendation> recommendations = request.fallbackRecommendations().stream()
            .map(this::withCatalogMatch)
            .toList();
        GearAdviceResponse response = new GearAdviceResponse(
            normalizedFingerprint,
            recommendations
        );
        artifactRepository.save(new GearAdviceArtifact(
            UUID.randomUUID().toString(),
            normalizedUserId,
            normalizedPlanId,
            response.assessmentFingerprint(),
            response.recommendations(),
            Instant.now(clock)
        ));
        return response;
    }

    private GearAdviceRecommendation withCatalogMatch(GearAdviceRecommendation recommendation) {
        validateRecommendation(recommendation);
        String matchedCatalogItemId = catalogRepository
            .search(recommendation.category(), "")
            .stream()
            .findFirst()
            .map(GearCatalogItem::catalogItemId)
            .orElse(null);

        return new GearAdviceRecommendation(
            recommendation.category().trim(),
            recommendation.status() == null ? GearAdviceStatus.CHECK : recommendation.status(),
            recommendation.rationale().trim(),
            matchedCatalogItemId
        );
    }

    private void validateRequest(String userId, String planId, GearAdviceRequest request) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User id is required.");
        }
        if (planId == null || planId.isBlank()) {
            throw new IllegalArgumentException("Plan id is required.");
        }
        if (request == null) {
            throw new IllegalArgumentException("Gear advice request is required.");
        }
        if (request.assessmentFingerprint() == null || request.assessmentFingerprint().isBlank()) {
            throw new IllegalArgumentException("Assessment fingerprint is required.");
        }
        if (request.fallbackRecommendations() == null || request.fallbackRecommendations().isEmpty()) {
            throw new IllegalArgumentException("Fallback recommendations are required.");
        }
    }

    private void validateRecommendation(GearAdviceRecommendation recommendation) {
        if (recommendation == null) {
            throw new IllegalArgumentException("Gear recommendation is required.");
        }
        if (recommendation.category() == null || recommendation.category().isBlank()) {
            throw new IllegalArgumentException("Gear recommendation category is required.");
        }
        if (recommendation.rationale() == null || recommendation.rationale().isBlank()) {
            throw new IllegalArgumentException("Gear recommendation rationale is required.");
        }
    }

}
