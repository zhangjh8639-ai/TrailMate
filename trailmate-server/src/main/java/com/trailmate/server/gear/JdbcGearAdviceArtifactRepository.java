package com.trailmate.server.gear;

import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

public class JdbcGearAdviceArtifactRepository implements GearAdviceArtifactRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcGearAdviceArtifactRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(GearAdviceArtifact artifact) {
        jdbcTemplate.update(
            """
                insert into gear_advice_artifact (
                    artifact_id, user_id, plan_id, assessment_fingerprint,
                    recommendations_payload, created_at
                ) values (?, ?, ?, ?, ?, ?)
                """,
            artifact.artifactId(),
            artifact.userId(),
            artifact.planId(),
            artifact.assessmentFingerprint(),
            encodeRecommendations(artifact.recommendations()),
            Timestamp.from(artifact.createdAt())
        );
    }

    @Override
    public Optional<GearAdviceArtifact> latestFor(
        String userId,
        String planId,
        String assessmentFingerprint
    ) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                """
                    select artifact_id, user_id, plan_id, assessment_fingerprint,
                           recommendations_payload, created_at
                    from gear_advice_artifact
                    where user_id = ?
                      and plan_id = ?
                      and assessment_fingerprint = ?
                    order by created_at desc
                    limit 1
                    """,
                (rs, rowNum) -> new GearAdviceArtifact(
                    rs.getString("artifact_id"),
                    rs.getString("user_id"),
                    rs.getString("plan_id"),
                    rs.getString("assessment_fingerprint"),
                    decodeRecommendations(rs.getString("recommendations_payload")),
                    rs.getTimestamp("created_at").toInstant()
                ),
                userId,
                planId,
                assessmentFingerprint
            ));
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    private String encodeRecommendations(List<GearAdviceRecommendation> recommendations) {
        Properties properties = new Properties();
        properties.setProperty("count", Integer.toString(recommendations.size()));
        for (int index = 0; index < recommendations.size(); index++) {
            GearAdviceRecommendation recommendation = recommendations.get(index);
            String prefix = "recommendation." + index;
            properties.setProperty(prefix + ".category", recommendation.category());
            properties.setProperty(prefix + ".status", recommendation.status().name());
            properties.setProperty(prefix + ".rationale", recommendation.rationale());
            properties.setProperty(prefix + ".matchedGearItemId", recommendation.matchedGearItemId() == null ? "" : recommendation.matchedGearItemId());
        }
        try {
            StringWriter writer = new StringWriter();
            properties.store(writer, "TrailMate gear advice artifact");
            return writer.toString();
        } catch (Exception exception) {
            throw new IllegalArgumentException("Gear advice recommendations could not be serialized.", exception);
        }
    }

    private List<GearAdviceRecommendation> decodeRecommendations(String payload) {
        try {
            Properties properties = new Properties();
            properties.load(new StringReader(payload));
            int count = Integer.parseInt(properties.getProperty("count", "0"));
            return java.util.stream.IntStream.range(0, count)
                .mapToObj(index -> {
                    String prefix = "recommendation." + index;
                    return new GearAdviceRecommendation(
                        properties.getProperty(prefix + ".category"),
                        GearAdviceStatus.valueOf(properties.getProperty(prefix + ".status")),
                        properties.getProperty(prefix + ".rationale"),
                        properties.getProperty(prefix + ".matchedGearItemId", "").isBlank()
                            ? null
                            : properties.getProperty(prefix + ".matchedGearItemId")
                    );
                })
                .toList();
        } catch (Exception exception) {
            throw new IllegalArgumentException("Gear advice recommendations could not be decoded.", exception);
        }
    }
}
