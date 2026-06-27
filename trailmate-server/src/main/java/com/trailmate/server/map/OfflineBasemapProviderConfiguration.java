package com.trailmate.server.map;

import java.nio.file.Path;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OfflineBasemapProviderConfiguration {
    @Bean
    @ConditionalOnMissingBean(OfflineBasemapCatalogRepository.class)
    public OfflineBasemapCatalogRepository offlineBasemapCatalogRepository() {
        return new InMemoryOfflineBasemapCatalogRepository();
    }

    @Bean
    @ConditionalOnMissingBean(OfflineBasemapService.class)
    public OfflineBasemapService offlineBasemapService(
        OfflineBasemapCatalogRepository offlineBasemapCatalogRepository,
        OfflineBasemapFileService offlineBasemapFileService
    ) {
        return new OfflineBasemapService(offlineBasemapCatalogRepository, offlineBasemapFileService);
    }

    @Bean
    @ConditionalOnMissingBean(OfflineBasemapFileService.class)
    public OfflineBasemapFileService offlineBasemapFileService(
        @Value("${trailmate.offline-basemap.pmtiles.directory:offline-basemaps/pmtiles}") String directory
    ) {
        return new OfflineBasemapFileService(Path.of(directory));
    }
}
