package com.trailmate.server.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

public class JdbcUserProfileRepository implements UserProfileRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcUserProfileRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public UserProfile save(UserProfile profile) {
        Timestamp updatedAt = Timestamp.from(profile.updatedAt());
        int updatedRows = jdbcTemplate.update(
            """
                update user_onboarding_profile
                set exercise_frequency = ?,
                    typical_duration = ?,
                    experience_level = ?,
                    ascent_experience = ?,
                    height_cm = ?,
                    weight_kg = ?,
                    common_pack_weight_kg = ?,
                    updated_at = ?
                where user_id = ?
                """,
            profile.exerciseFrequency(),
            profile.typicalDuration(),
            profile.experienceLevel(),
            profile.ascentExperience(),
            profile.heightCm(),
            profile.weightKg(),
            profile.commonPackWeightKg(),
            updatedAt,
            profile.userId()
        );
        if (updatedRows == 0) {
            jdbcTemplate.update(
                """
                    insert into user_onboarding_profile (
                        user_id, exercise_frequency, typical_duration, experience_level,
                        ascent_experience, height_cm, weight_kg, common_pack_weight_kg,
                        created_at, updated_at
                    ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                profile.userId(),
                profile.exerciseFrequency(),
                profile.typicalDuration(),
                profile.experienceLevel(),
                profile.ascentExperience(),
                profile.heightCm(),
                profile.weightKg(),
                profile.commonPackWeightKg(),
                updatedAt,
                updatedAt
            );
        }
        jdbcTemplate.update(
            """
                update app_user
                set onboarding_status = 'profile_completed',
                    updated_at = ?
                where id = ?
                  and onboarding_status = 'account_created'
                """,
            updatedAt,
            profile.userId()
        );
        return findByUserId(profile.userId()).orElse(profile);
    }

    @Override
    public Optional<UserProfile> findByUserId(String userId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                """
                    select user_id, exercise_frequency, typical_duration, experience_level,
                           ascent_experience, height_cm, weight_kg, common_pack_weight_kg,
                           updated_at
                    from user_onboarding_profile
                    where user_id = ?
                    """,
                (rs, rowNum) -> mapProfile(rs),
                userId
            ));
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    private UserProfile mapProfile(ResultSet rs) throws SQLException {
        return new UserProfile(
            rs.getString("user_id"),
            rs.getString("exercise_frequency"),
            rs.getString("typical_duration"),
            rs.getString("experience_level"),
            rs.getString("ascent_experience"),
            nullableInteger(rs, "height_cm"),
            nullableInteger(rs, "weight_kg"),
            nullableInteger(rs, "common_pack_weight_kg"),
            rs.getTimestamp("updated_at").toInstant()
        );
    }

    private Integer nullableInteger(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }
}
