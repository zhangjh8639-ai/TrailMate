package com.trailmate.server.user;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdbcUserProfileRepositoryTest {
    @Test
    void saveUpsertsProfileAndMarksUserProfileCompleted() throws Exception {
        try (
            Connection connection = DriverManager.getConnection(
                "jdbc:h2:mem:user_profile_repo;MODE=PostgreSQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1"
            )
        ) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(new SingleConnectionDataSource(connection, true));
            createSchema(jdbcTemplate);
            JdbcUserProfileRepository repository = new JdbcUserProfileRepository(jdbcTemplate);

            UserProfile saved = repository.save(new UserProfile(
                "usr_123",
                "ONE_TO_TWO_PER_WEEK",
                "OVER_60",
                "REGULAR",
                "M300_TO_800",
                178,
                70,
                6,
                Instant.parse("2026-06-23T08:00:00Z")
            ));

            Optional<UserProfile> loaded = repository.findByUserId("usr_123");
            String onboardingStatus = jdbcTemplate.queryForObject(
                "select onboarding_status from app_user where id = 'usr_123'",
                String.class
            );

            assertEquals("usr_123", saved.userId());
            assertTrue(loaded.isPresent());
            assertEquals("REGULAR", loaded.get().experienceLevel());
            assertEquals("profile_completed", onboardingStatus);
        }
    }

    private void createSchema(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("""
            create table app_user (
                id text primary key,
                onboarding_status text not null default 'account_created',
                updated_at timestamp not null
            )
            """);
        jdbcTemplate.execute("insert into app_user (id, updated_at) values ('usr_123', current_timestamp)");
        jdbcTemplate.execute("""
            create table user_onboarding_profile (
                user_id text primary key references app_user(id),
                exercise_frequency text not null,
                typical_duration text not null,
                experience_level text not null,
                ascent_experience text not null,
                height_cm integer,
                weight_kg integer,
                common_pack_weight_kg integer,
                created_at timestamp not null,
                updated_at timestamp not null
            )
            """);
    }
}
