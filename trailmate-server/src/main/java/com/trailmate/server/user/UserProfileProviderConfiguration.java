package com.trailmate.server.user;

import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class UserProfileProviderConfiguration {
    @Bean
    @ConditionalOnProperty(prefix = "trailmate.user-profile.persistence", name = "mode", havingValue = "jdbc")
    public UserProfileRepository jdbcUserProfileRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcUserProfileRepository(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(UserProfileRepository.class)
    public UserProfileRepository userProfileRepository() {
        return new InMemoryUserProfileRepository();
    }

    @Bean
    public UserProfileService userProfileService(
        UserProfileRepository userProfileRepository,
        Clock clock
    ) {
        return new UserProfileService(userProfileRepository, clock);
    }
}
