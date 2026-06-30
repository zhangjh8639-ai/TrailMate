package com.trailmate.server.gear;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdbcGearAdviceArtifactRepositoryTest {
    private JdbcGearAdviceArtifactRepository repository;

    @BeforeEach
    void setUp() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:trailmate_gear_advice;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("drop table if exists gear_advice_artifact");
        jdbcTemplate.execute("""
            create table gear_advice_artifact (
                artifact_id varchar(120) primary key,
                user_id varchar(120) not null,
                plan_id varchar(240) not null,
                assessment_fingerprint varchar(500) not null,
                recommendations_payload clob not null,
                created_at timestamp not null
            )
            """);
        repository = new JdbcGearAdviceArtifactRepository(jdbcTemplate);
    }

    @Test
    void latestForRoundTripsRecommendationsPayload() {
        GearAdviceArtifact artifact = new GearAdviceArtifact(
            "artifact-1",
            "usr-1",
            "plan-123",
            "fp-longjing-1",
            List.of(
                new GearAdviceRecommendation(
                    "头灯",
                    GearAdviceStatus.CHECK,
                    "确认电量并准备备用照明。",
                    "cat_headlamp_bd_spot_400"
                )
            ),
            Instant.parse("2026-06-30T08:00:00Z")
        );

        repository.save(artifact);

        var loaded = repository.latestFor("usr-1", "plan-123", "fp-longjing-1");
        assertTrue(loaded.isPresent());
        assertEquals(artifact.recommendations(), loaded.get().recommendations());
    }
}
