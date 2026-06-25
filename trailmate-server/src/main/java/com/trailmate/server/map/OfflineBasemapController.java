package com.trailmate.server.map;

import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/offline-basemaps")
public class OfflineBasemapController {
    private final OfflineBasemapService offlineBasemapService;

    public OfflineBasemapController(OfflineBasemapService offlineBasemapService) {
        this.offlineBasemapService = offlineBasemapService;
    }

    @GetMapping("/pmtiles/catalog")
    public List<OfflineBasemapCatalogItem> listPmTilesCatalog(
        @RequestParam double minLongitude,
        @RequestParam double minLatitude,
        @RequestParam double maxLongitude,
        @RequestParam double maxLatitude
    ) {
        return offlineBasemapService.listPmTilesCatalog(
            minLongitude,
            minLatitude,
            maxLongitude,
            maxLatitude
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<OfflineBasemapApiErrorResponse> handleIllegalArgument(
        IllegalArgumentException exception
    ) {
        return ResponseEntity
            .badRequest()
            .body(new OfflineBasemapApiErrorResponse(
                400,
                "OFFLINE_BASEMAP_INVALID_BOUNDS",
                exception.getMessage() == null ? "Invalid offline basemap request." : exception.getMessage(),
                UUID.randomUUID().toString()
            ));
    }
}
