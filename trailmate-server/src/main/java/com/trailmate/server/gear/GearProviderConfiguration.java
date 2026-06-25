package com.trailmate.server.gear;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class GearProviderConfiguration {
    @Bean
    @ConditionalOnProperty(prefix = "trailmate.gear.persistence", name = "mode", havingValue = "jdbc")
    public GearCatalogRepository jdbcGearCatalogRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcGearCatalogRepository(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(GearCatalogRepository.class)
    public GearCatalogRepository gearCatalogRepository() {
        return new InMemoryGearCatalogRepository();
    }

    @Bean
    @ConditionalOnMissingBean(GearService.class)
    public GearService gearService(GearCatalogRepository gearCatalogRepository) {
        return new GearService(gearCatalogRepository);
    }
}
