package com.trailmate.server.gear;

import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/gear")
public class GearController {
    private final GearService gearService;

    public GearController(GearService gearService) {
        this.gearService = gearService;
    }

    @GetMapping("/catalog/categories")
    public List<String> listCategories() {
        return gearService.listCategories();
    }

    @GetMapping("/catalog/search")
    public List<GearCatalogItem> searchCatalog(
        @RequestParam(required = false, defaultValue = "") String category,
        @RequestParam(required = false, defaultValue = "") String q,
        @RequestParam(required = false, defaultValue = "") String query
    ) {
        return gearService.searchCatalog(category, q.isBlank() ? query : q);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GearApiErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity
            .badRequest()
            .body(new GearApiErrorResponse(
                400,
                "GEAR_INVALID_REQUEST",
                exception.getMessage() == null ? "Invalid gear request." : exception.getMessage(),
                UUID.randomUUID().toString()
            ));
    }
}
