# TrailMate Production Readiness V2 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build direction A: a production-ready TrailMate flow with mature auth/onboarding copy, route/full-screen navigation separation, server-backed gear catalog selection, remote-linux deployment, and SM_S9260 true-device validation.

**Architecture:** Add a focused Spring Boot gear catalog package on the server, expose catalog and inventory endpoints, then consume those contracts in Android through small DTO/client/model units. Keep route field navigation focused by introducing presentation policies before changing Compose screens. Use local catalog fallback only when the server is unavailable; user inventory creation must reference server catalog items rather than free-form brand/model input.

**Tech Stack:** Java 17 Spring Boot server, JDBC/Flyway-ready data model, Kotlin Android Compose client, JUnit/MockMvc/Compose tests, Docker Compose deployment, adb true-device QA.

---

## File Structure

### Server Gear Catalog

- Create `trailmate-server/src/main/java/com/trailmate/server/gear/GearCatalogItem.java`
  - Immutable catalog DTO.
- Create `trailmate-server/src/main/java/com/trailmate/server/gear/GearInventoryItem.java`
  - Immutable user inventory DTO.
- Create `trailmate-server/src/main/java/com/trailmate/server/gear/GearInventoryCreateRequest.java`
  - Request for adding a catalog item to inventory.
- Create `trailmate-server/src/main/java/com/trailmate/server/gear/GearInventoryUpdateRequest.java`
  - Request for availability updates.
- Create `trailmate-server/src/main/java/com/trailmate/server/gear/GearCatalogRepository.java`
  - Catalog lookup interface.
- Create `trailmate-server/src/main/java/com/trailmate/server/gear/InMemoryGearCatalogRepository.java`
  - Seed catalog for MVP and tests.
- Create `trailmate-server/src/main/java/com/trailmate/server/gear/GearInventoryRepository.java`
  - User inventory persistence interface.
- Create `trailmate-server/src/main/java/com/trailmate/server/gear/InMemoryGearInventoryRepository.java`
  - In-memory inventory repository for MVP server runtime.
- Create `trailmate-server/src/main/java/com/trailmate/server/gear/GearService.java`
  - Business logic for category listing, search, inventory create/update/delete.
- Create `trailmate-server/src/main/java/com/trailmate/server/gear/GearController.java`
  - REST endpoints under `/api/v1/gear`.
- Test `trailmate-server/src/test/java/com/trailmate/server/gear/GearServiceTest.java`
- Test `trailmate-server/src/test/java/com/trailmate/server/gear/GearControllerTest.java`

### Android Catalog Contract And Client

- Modify `android-app/src/main/java/com/trailmate/app/core/network/TrailMateServerApiContract.kt`
  - Add endpoint constants and DTOs.
- Create `android-app/src/main/java/com/trailmate/app/core/network/TrailMateGearCatalogApi.kt`
  - Interface for catalog categories/search/inventory.
- Create `android-app/src/main/java/com/trailmate/app/core/network/TrailMateHttpGearCatalogApiClient.kt`
  - HTTP client using `HttpURLConnection`, matching existing client style.
- Create `android-app/src/main/java/com/trailmate/app/core/model/GearCatalogModels.kt`
  - UI/domain models for catalog and inventory selection.
- Create `android-app/src/main/java/com/trailmate/app/core/model/GearCatalogSelectionEngine.kt`
  - Pure model logic for matching selected catalog item to route gear recommendation.
- Test `android-app/src/test/java/com/trailmate/app/core/network/TrailMateGearCatalogApiContractTest.kt`
- Test `android-app/src/test/java/com/trailmate/app/core/model/GearCatalogSelectionEngineTest.kt`

### Android Gear UI

- Modify `android-app/src/main/java/com/trailmate/app/feature/gear/MyGearScreen.kt`
  - Replace free-form add panel with server catalog selection UI.
  - Do not expose phone-side free-form brand/model creation.
- Create `android-app/src/main/java/com/trailmate/app/feature/gear/GearCatalogSearchUiState.kt`
  - Pure UI state/reducer for search, loading, success, error, selected item.
- Test `android-app/src/test/java/com/trailmate/app/feature/gear/GearCatalogSearchUiStateTest.kt`
- Update `android-app/src/androidTest/java/com/trailmate/app/TrailMateAppSmokeTest.kt`
  - Assert missing route gear action says `选择装备`, not primary `添加已有装备`.

### Navigation Separation

- Create `android-app/src/main/java/com/trailmate/app/core/model/FullscreenNavigationSurfacePolicy.kt`
  - Pure policy for what may appear in first viewport.
- Test `android-app/src/test/java/com/trailmate/app/core/model/FullscreenNavigationSurfacePolicyTest.kt`
- Modify `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`
  - Keep full-screen mode field-focused and move diagnostics behind secondary disclosure.
- Update or add route UI smoke assertions in `android-app/src/androidTest/java/com/trailmate/app/TrailMateAppSmokeTest.kt`.

### Docs, Deployment, QA

- Modify `docs/api/trailmate-server-api.md`
  - Add catalog and inventory endpoints.
- Modify `docs/deployment/ubuntu-docker-compose.md`
  - Add deployed catalog smoke checks.
- Modify `openspec/changes/trailmate-production-readiness-v2/tasks.md`
  - Check off implementation tasks as each completes.
- Add QA notes under `outputs/qa-artifacts/2026-06-23/` only; this path is ignored by git.

---

## Task 1: Server Gear Catalog Domain

**Files:**
- Create: `trailmate-server/src/test/java/com/trailmate/server/gear/GearServiceTest.java`
- Create: `trailmate-server/src/main/java/com/trailmate/server/gear/GearCatalogItem.java`
- Create: `trailmate-server/src/main/java/com/trailmate/server/gear/GearInventoryItem.java`
- Create: `trailmate-server/src/main/java/com/trailmate/server/gear/GearInventoryCreateRequest.java`
- Create: `trailmate-server/src/main/java/com/trailmate/server/gear/GearInventoryUpdateRequest.java`
- Create: `trailmate-server/src/main/java/com/trailmate/server/gear/GearCatalogRepository.java`
- Create: `trailmate-server/src/main/java/com/trailmate/server/gear/InMemoryGearCatalogRepository.java`
- Create: `trailmate-server/src/main/java/com/trailmate/server/gear/GearInventoryRepository.java`
- Create: `trailmate-server/src/main/java/com/trailmate/server/gear/InMemoryGearInventoryRepository.java`
- Create: `trailmate-server/src/main/java/com/trailmate/server/gear/GearService.java`

- [ ] **Step 1: Write failing service tests**

Create `trailmate-server/src/test/java/com/trailmate/server/gear/GearServiceTest.java`:

```java
package com.trailmate.server.gear;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GearServiceTest {
    private final GearService service = new GearService(
        new InMemoryGearCatalogRepository(),
        new InMemoryGearInventoryRepository()
    );

    @Test
    void listCategoriesReturnsStableOutdoorCategories() {
        List<String> categories = service.listCategories();

        assertTrue(categories.contains("雨衣（防水透气）"));
        assertTrue(categories.contains("头灯"));
        assertTrue(categories.contains("登山杖"));
    }

    @Test
    void searchFindsCatalogItemsByCategoryAndQuery() {
        List<GearCatalogItem> results = service.searchCatalog("雨衣（防水透气）", "beta");

        assertFalse(results.isEmpty());
        GearCatalogItem item = results.get(0);
        assertEquals("cat_rain_arcteryx_beta_lt", item.catalogItemId());
        assertEquals("Arc'teryx", item.brand());
        assertEquals("Beta LT Jacket", item.model());
        assertEquals("雨衣（防水透气）", item.category());
    }

    @Test
    void saveCatalogItemCreatesUserInventoryItem() {
        GearInventoryItem item = service.saveInventoryItem(
            "usr_123",
            new GearInventoryCreateRequest("cat_headlamp_bd_spot_400", null, null, null, true)
        );

        assertEquals("cat_headlamp_bd_spot_400", item.catalogItemId());
        assertEquals("头灯", item.category());
        assertEquals("Black Diamond", item.brand());
        assertEquals("Spot 400", item.model());
        assertEquals(true, item.available());
        assertEquals(false, item.custom());
    }

    @Test
    void saveCustomItemKeepsCustomFlag() {
        GearInventoryItem item = service.saveInventoryItem(
            "usr_123",
            new GearInventoryCreateRequest(null, "保温层（抓绒/羽绒）", "Montbell", "自定义羽绒服", true)
        );

        assertEquals(null, item.catalogItemId());
        assertEquals("保温层（抓绒/羽绒）", item.category());
        assertEquals("Montbell", item.brand());
        assertEquals("自定义羽绒服", item.model());
        assertEquals(true, item.custom());
    }

    @Test
    void updateAvailabilityChangesOwnedItemOnly() {
        GearInventoryItem created = service.saveInventoryItem(
            "usr_123",
            new GearInventoryCreateRequest("cat_poles_leki_legacy_lite", null, null, null, true)
        );

        GearInventoryItem updated = service.updateInventoryItem(
            "usr_123",
            created.inventoryItemId(),
            new GearInventoryUpdateRequest(false)
        );

        assertEquals(created.inventoryItemId(), updated.inventoryItemId());
        assertEquals(false, updated.available());
    }
}
```

- [ ] **Step 2: Run service tests to verify red**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat :trailmate-server:test --tests "com.trailmate.server.gear.GearServiceTest" --console=plain
```

Expected: compilation fails because `GearService` and related classes do not exist.

- [ ] **Step 3: Implement catalog and inventory records**

Create `GearCatalogItem.java`:

```java
package com.trailmate.server.gear;

import java.util.List;

public record GearCatalogItem(
    String catalogItemId,
    String category,
    String brand,
    String model,
    String displayName,
    Integer weightGrams,
    List<String> tags,
    String source
) { }
```

Create `GearInventoryItem.java`:

```java
package com.trailmate.server.gear;

public record GearInventoryItem(
    String inventoryItemId,
    String userId,
    String catalogItemId,
    String category,
    String brand,
    String model,
    String displayName,
    Integer weightGrams,
    boolean available,
    boolean custom
) { }
```

Create `GearInventoryCreateRequest.java`:

```java
package com.trailmate.server.gear;

public record GearInventoryCreateRequest(
    String catalogItemId,
    String category,
    String brand,
    String model,
    boolean available
) { }
```

Create `GearInventoryUpdateRequest.java`:

```java
package com.trailmate.server.gear;

public record GearInventoryUpdateRequest(boolean available) { }
```

- [ ] **Step 4: Implement repositories**

Create `GearCatalogRepository.java`:

```java
package com.trailmate.server.gear;

import java.util.List;
import java.util.Optional;

public interface GearCatalogRepository {
    List<String> listCategories();
    List<GearCatalogItem> search(String category, String query);
    Optional<GearCatalogItem> findById(String catalogItemId);
}
```

Create `InMemoryGearCatalogRepository.java`:

```java
package com.trailmate.server.gear;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class InMemoryGearCatalogRepository implements GearCatalogRepository {
    private final List<GearCatalogItem> items = List.of(
        new GearCatalogItem(
            "cat_rain_arcteryx_beta_lt",
            "雨衣（防水透气）",
            "Arc'teryx",
            "Beta LT Jacket",
            "Arc'teryx Beta LT Jacket",
            395,
            List.of("防水", "硬壳", "暴露路段"),
            "seed"
        ),
        new GearCatalogItem(
            "cat_headlamp_bd_spot_400",
            "头灯",
            "Black Diamond",
            "Spot 400",
            "Black Diamond Spot 400",
            78,
            List.of("夜间", "备用电池", "安全"),
            "seed"
        ),
        new GearCatalogItem(
            "cat_poles_leki_legacy_lite",
            "登山杖",
            "Leki",
            "Legacy Lite AS",
            "Leki Legacy Lite AS",
            510,
            List.of("长距离", "下坡", "稳定"),
            "seed"
        ),
        new GearCatalogItem(
            "cat_insulation_montbell_plasma",
            "保温层（抓绒/羽绒）",
            "Montbell",
            "Plasma 1000 Down Jacket",
            "Montbell Plasma 1000 Down Jacket",
            130,
            List.of("保暖", "轻量", "高海拔"),
            "seed"
        ),
        new GearCatalogItem(
            "cat_water_source_hydrapak_2l",
            "备用水",
            "Hydrapak",
            "Seeker 2L",
            "Hydrapak Seeker 2L",
            76,
            List.of("补水", "长距离", "可压缩"),
            "seed"
        )
    );

    @Override
    public List<String> listCategories() {
        return items.stream()
            .map(GearCatalogItem::category)
            .distinct()
            .sorted()
            .toList();
    }

    @Override
    public List<GearCatalogItem> search(String category, String query) {
        String normalizedCategory = normalize(category);
        String normalizedQuery = normalize(query);
        return items.stream()
            .filter(item -> normalizedCategory.isBlank() || normalize(item.category()).equals(normalizedCategory))
            .filter(item -> normalizedQuery.isBlank() || searchableText(item).contains(normalizedQuery))
            .sorted(Comparator.comparing(GearCatalogItem::displayName))
            .toList();
    }

    @Override
    public Optional<GearCatalogItem> findById(String catalogItemId) {
        return items.stream()
            .filter(item -> item.catalogItemId().equals(catalogItemId))
            .findFirst();
    }

    private String searchableText(GearCatalogItem item) {
        return normalize(String.join(" ", item.category(), item.brand(), item.model(), item.displayName(), String.join(" ", item.tags())));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
```

Create `GearInventoryRepository.java`:

```java
package com.trailmate.server.gear;

import java.util.List;
import java.util.Optional;

public interface GearInventoryRepository {
    GearInventoryItem save(GearInventoryItem item);
    List<GearInventoryItem> listByUser(String userId);
    Optional<GearInventoryItem> findByUserAndId(String userId, String inventoryItemId);
    void deleteByUserAndId(String userId, String inventoryItemId);
}
```

Create `InMemoryGearInventoryRepository.java`:

```java
package com.trailmate.server.gear;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryGearInventoryRepository implements GearInventoryRepository {
    private final Map<String, GearInventoryItem> itemsById = new LinkedHashMap<>();

    @Override
    public synchronized GearInventoryItem save(GearInventoryItem item) {
        itemsById.put(item.inventoryItemId(), item);
        return item;
    }

    @Override
    public synchronized List<GearInventoryItem> listByUser(String userId) {
        List<GearInventoryItem> result = new ArrayList<>();
        for (GearInventoryItem item : itemsById.values()) {
            if (item.userId().equals(userId)) {
                result.add(item);
            }
        }
        return result;
    }

    @Override
    public synchronized Optional<GearInventoryItem> findByUserAndId(String userId, String inventoryItemId) {
        GearInventoryItem item = itemsById.get(inventoryItemId);
        if (item == null || !item.userId().equals(userId)) {
            return Optional.empty();
        }
        return Optional.of(item);
    }

    @Override
    public synchronized void deleteByUserAndId(String userId, String inventoryItemId) {
        findByUserAndId(userId, inventoryItemId).ifPresent(item -> itemsById.remove(inventoryItemId));
    }
}
```

- [ ] **Step 5: Implement service**

Create `GearService.java`:

```java
package com.trailmate.server.gear;

import java.util.List;
import java.util.UUID;

public class GearService {
    private final GearCatalogRepository catalogRepository;
    private final GearInventoryRepository inventoryRepository;

    public GearService(
        GearCatalogRepository catalogRepository,
        GearInventoryRepository inventoryRepository
    ) {
        this.catalogRepository = catalogRepository;
        this.inventoryRepository = inventoryRepository;
    }

    public List<String> listCategories() {
        return catalogRepository.listCategories();
    }

    public List<GearCatalogItem> searchCatalog(String category, String query) {
        return catalogRepository.search(category, query);
    }

    public List<GearInventoryItem> listInventory(String userId) {
        return inventoryRepository.listByUser(userId);
    }

    public GearInventoryItem saveInventoryItem(String userId, GearInventoryCreateRequest request) {
        if (request.catalogItemId() != null && !request.catalogItemId().isBlank()) {
            GearCatalogItem catalogItem = catalogRepository.findById(request.catalogItemId())
                .orElseThrow(() -> new IllegalArgumentException("Gear catalog item not found."));
            return inventoryRepository.save(new GearInventoryItem(
                nextInventoryId(),
                userId,
                catalogItem.catalogItemId(),
                catalogItem.category(),
                catalogItem.brand(),
                catalogItem.model(),
                catalogItem.displayName(),
                catalogItem.weightGrams(),
                request.available(),
                false
            ));
        }
        if (isBlank(request.category()) || isBlank(request.model())) {
            throw new IllegalArgumentException("Custom gear requires category and model.");
        }
        String brand = request.brand() == null ? "" : request.brand().trim();
        String model = request.model().trim();
        String displayName = String.join(" ", brand, model).trim();
        return inventoryRepository.save(new GearInventoryItem(
            nextInventoryId(),
            userId,
            null,
            request.category().trim(),
            brand,
            model,
            displayName.isBlank() ? model : displayName,
            null,
            request.available(),
            true
        ));
    }

    public GearInventoryItem updateInventoryItem(
        String userId,
        String inventoryItemId,
        GearInventoryUpdateRequest request
    ) {
        GearInventoryItem current = inventoryRepository.findByUserAndId(userId, inventoryItemId)
            .orElseThrow(() -> new IllegalArgumentException("Gear inventory item not found."));
        GearInventoryItem updated = new GearInventoryItem(
            current.inventoryItemId(),
            current.userId(),
            current.catalogItemId(),
            current.category(),
            current.brand(),
            current.model(),
            current.displayName(),
            current.weightGrams(),
            request.available(),
            current.custom()
        );
        return inventoryRepository.save(updated);
    }

    public void deleteInventoryItem(String userId, String inventoryItemId) {
        inventoryRepository.deleteByUserAndId(userId, inventoryItemId);
    }

    private String nextInventoryId() {
        return "gear_" + UUID.randomUUID().toString().replace("-", "");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
```

- [ ] **Step 6: Run service tests to verify green**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat :trailmate-server:test --tests "com.trailmate.server.gear.GearServiceTest" --console=plain
```

Expected: `BUILD SUCCESSFUL`.

---

## Task 2: Server Gear Catalog REST API

**Files:**
- Create: `trailmate-server/src/test/java/com/trailmate/server/gear/GearControllerTest.java`
- Create: `trailmate-server/src/main/java/com/trailmate/server/gear/GearController.java`
- Modify: `trailmate-server/src/main/java/com/trailmate/server/TrailMateServerApplication.java`

- [ ] **Step 1: Write failing controller tests**

Create `GearControllerTest.java`:

```java
package com.trailmate.server.gear;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GearControllerTest {
    private final GearService service = new GearService(
        new InMemoryGearCatalogRepository(),
        new InMemoryGearInventoryRepository()
    );
    private final MockMvc mockMvc = MockMvcBuilders
        .standaloneSetup(new GearController(service))
        .build();

    @Test
    void categoriesEndpointReturnsCatalogCategories() throws Exception {
        mockMvc.perform(get("/api/v1/gear/catalog/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0]").exists());
    }

    @Test
    void catalogSearchEndpointReturnsMatchingItems() throws Exception {
        mockMvc.perform(get("/api/v1/gear/catalog/search")
                .param("category", "雨衣（防水透气）")
                .param("q", "beta"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].catalogItemId").value("cat_rain_arcteryx_beta_lt"))
            .andExpect(jsonPath("$[0].brand").value("Arc'teryx"));
    }

    @Test
    void inventoryEndpointSavesCatalogItem() throws Exception {
        mockMvc.perform(post("/api/v1/gear/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "catalogItemId": "cat_headlamp_bd_spot_400",
                      "available": true
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.catalogItemId").value("cat_headlamp_bd_spot_400"))
            .andExpect(jsonPath("$.custom").value(false));
    }

    @Test
    void inventoryEndpointListsSavedItems() throws Exception {
        service.saveInventoryItem(
            "local-preview-user",
            new GearInventoryCreateRequest("cat_headlamp_bd_spot_400", null, null, null, true)
        );

        mockMvc.perform(get("/api/v1/gear/inventory"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].displayName").value("Black Diamond Spot 400"));
    }

    @Test
    void updateInventoryEndpointChangesAvailability() throws Exception {
        GearInventoryItem item = service.saveInventoryItem(
            "local-preview-user",
            new GearInventoryCreateRequest("cat_poles_leki_legacy_lite", null, null, null, true)
        );

        mockMvc.perform(patch("/api/v1/gear/inventory/" + item.inventoryItemId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "available": false
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void deleteInventoryEndpointRemovesItem() throws Exception {
        GearInventoryItem item = service.saveInventoryItem(
            "local-preview-user",
            new GearInventoryCreateRequest("cat_poles_leki_legacy_lite", null, null, null, true)
        );

        mockMvc.perform(delete("/api/v1/gear/inventory/" + item.inventoryItemId()))
            .andExpect(status().isNoContent());
    }
}
```

- [ ] **Step 2: Run controller tests to verify red**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat :trailmate-server:test --tests "com.trailmate.server.gear.GearControllerTest" --console=plain
```

Expected: compilation fails because `GearController` does not exist.

- [ ] **Step 3: Implement controller**

Create `GearController.java`:

```java
package com.trailmate.server.gear;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/gear")
public class GearController {
    static final String PREVIEW_USER_ID = "local-preview-user";

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
        @RequestParam(required = false, defaultValue = "") String q
    ) {
        return gearService.searchCatalog(category, q);
    }

    @GetMapping("/inventory")
    public List<GearInventoryItem> listInventory() {
        return gearService.listInventory(PREVIEW_USER_ID);
    }

    @PostMapping("/inventory")
    public GearInventoryItem saveInventoryItem(@Valid @RequestBody GearInventoryCreateRequest request) {
        return gearService.saveInventoryItem(PREVIEW_USER_ID, request);
    }

    @PatchMapping("/inventory/{inventoryItemId}")
    public GearInventoryItem updateInventoryItem(
        @PathVariable String inventoryItemId,
        @Valid @RequestBody GearInventoryUpdateRequest request
    ) {
        return gearService.updateInventoryItem(PREVIEW_USER_ID, inventoryItemId, request);
    }

    @DeleteMapping("/inventory/{inventoryItemId}")
    public ResponseEntity<Void> deleteInventoryItem(@PathVariable String inventoryItemId) {
        gearService.deleteInventoryItem(PREVIEW_USER_ID, inventoryItemId);
        return ResponseEntity.noContent().build();
    }
}
```

- [ ] **Step 4: Register service beans**

Modify `TrailMateServerApplication.java` to include beans:

```java
package com.trailmate.server;

import com.trailmate.server.gear.GearCatalogRepository;
import com.trailmate.server.gear.GearInventoryRepository;
import com.trailmate.server.gear.GearService;
import com.trailmate.server.gear.InMemoryGearCatalogRepository;
import com.trailmate.server.gear.InMemoryGearInventoryRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TrailMateServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(TrailMateServerApplication.class, args);
    }

    @Bean
    GearCatalogRepository gearCatalogRepository() {
        return new InMemoryGearCatalogRepository();
    }

    @Bean
    GearInventoryRepository gearInventoryRepository() {
        return new InMemoryGearInventoryRepository();
    }

    @Bean
    GearService gearService(
        GearCatalogRepository gearCatalogRepository,
        GearInventoryRepository gearInventoryRepository
    ) {
        return new GearService(gearCatalogRepository, gearInventoryRepository);
    }
}
```

If the file already has imports or other bean methods, merge these imports and methods without removing existing code.

- [ ] **Step 5: Run server tests**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat :trailmate-server:test --tests "com.trailmate.server.gear.*" --console=plain
```

Expected: `BUILD SUCCESSFUL`.

---

## Task 3: Android Gear Catalog Contract And Matching Engine

**Files:**
- Modify: `android-app/src/main/java/com/trailmate/app/core/network/TrailMateServerApiContract.kt`
- Create: `android-app/src/main/java/com/trailmate/app/core/model/GearCatalogModels.kt`
- Create: `android-app/src/main/java/com/trailmate/app/core/model/GearCatalogSelectionEngine.kt`
- Create: `android-app/src/test/java/com/trailmate/app/core/network/TrailMateGearCatalogApiContractTest.kt`
- Create: `android-app/src/test/java/com/trailmate/app/core/model/GearCatalogSelectionEngineTest.kt`

- [ ] **Step 1: Write failing contract test**

Create `TrailMateGearCatalogApiContractTest.kt`:

```kotlin
package com.trailmate.app.core.network

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailMateGearCatalogApiContractTest {
    @Test
    fun exposesGearCatalogAndInventoryEndpoints() {
        assertEquals("/gear/catalog/categories", TrailMateServerApiContract.Endpoints.gearCatalogCategories)
        assertEquals("/gear/catalog/search", TrailMateServerApiContract.Endpoints.gearCatalogSearch)
        assertEquals("/gear/inventory", TrailMateServerApiContract.Endpoints.gearInventory)
    }

    @Test
    fun catalogDtoCarriesServerOwnedGearFields() {
        val dto = TrailMateGearCatalogItemDto(
            catalogItemId = "cat_rain_arcteryx_beta_lt",
            category = "雨衣（防水透气）",
            brand = "Arc'teryx",
            model = "Beta LT Jacket",
            displayName = "Arc'teryx Beta LT Jacket",
            weightGrams = 395,
            tags = listOf("防水", "硬壳"),
            source = "seed"
        )

        assertEquals("cat_rain_arcteryx_beta_lt", dto.catalogItemId)
        assertEquals("Arc'teryx Beta LT Jacket", dto.displayName)
        assertEquals(false, dto.tags.isEmpty())
    }
}
```

- [ ] **Step 2: Run contract test to verify red**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.network.TrailMateGearCatalogApiContractTest" --console=plain
```

Expected: compilation fails because endpoint constants and DTO do not exist.

- [ ] **Step 3: Add endpoint constants and DTOs**

Modify `TrailMateServerApiContract.kt` inside `Endpoints`:

```kotlin
const val gearCatalogCategories = "/gear/catalog/categories"
const val gearCatalogSearch = "/gear/catalog/search"
const val gearInventory = "/gear/inventory"
```

Add DTOs near existing gear DTOs:

```kotlin
data class TrailMateGearCatalogItemDto(
    val catalogItemId: String,
    val category: String,
    val brand: String,
    val model: String,
    val displayName: String,
    val weightGrams: Int?,
    val tags: List<String>,
    val source: String
)

data class TrailMateGearInventoryItemDto(
    val inventoryItemId: String,
    val catalogItemId: String?,
    val category: String,
    val brand: String?,
    val model: String,
    val displayName: String,
    val weightGrams: Int?,
    val available: Boolean,
    val custom: Boolean
)

data class TrailMateGearInventoryCreateRequestDto(
    val catalogItemId: String?,
    val category: String?,
    val brand: String?,
    val model: String?,
    val available: Boolean
)

data class TrailMateGearInventoryUpdateRequestDto(
    val available: Boolean
)
```

- [ ] **Step 4: Run contract test to verify green**

Run the same test command. Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Write failing matching engine test**

Create `GearCatalogSelectionEngineTest.kt`:

```kotlin
package com.trailmate.app.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GearCatalogSelectionEngineTest {
    @Test
    fun catalogItemSelectionCreatesAvailableInventoryItem() {
        val item = GearCatalogItem(
            catalogItemId = "cat_headlamp_bd_spot_400",
            category = "头灯",
            brand = "Black Diamond",
            model = "Spot 400",
            displayName = "Black Diamond Spot 400",
            weightGrams = 78,
            tags = listOf("夜间"),
            source = "seed"
        )

        val inventoryItem = GearCatalogSelectionEngine.toInventoryItem(item)

        assertEquals("cat_headlamp_bd_spot_400", inventoryItem.catalogItemId)
        assertEquals("头灯", inventoryItem.category)
        assertEquals("Black Diamond Spot 400", inventoryItem.displayName)
        assertEquals(true, inventoryItem.available)
        assertEquals(false, inventoryItem.custom)
    }

    @Test
    fun selectedCatalogItemCoversSameCategoryRecommendation() {
        val item = GearCatalogSelectionEngine.toInventoryItem(
            GearCatalogItem(
                catalogItemId = "cat_rain_arcteryx_beta_lt",
                category = "雨衣（防水透气）",
                brand = "Arc'teryx",
                model = "Beta LT Jacket",
                displayName = "Arc'teryx Beta LT Jacket",
                weightGrams = 395,
                tags = emptyList(),
                source = "seed"
            )
        )

        assertTrue(GearCatalogSelectionEngine.coversCategory(item, "雨衣（防水透气）"))
    }
}
```

- [ ] **Step 6: Run matching test to verify red**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.model.GearCatalogSelectionEngineTest" --console=plain
```

Expected: compilation fails because `GearCatalogItem` and engine do not exist.

- [ ] **Step 7: Implement catalog models and engine**

Create `GearCatalogModels.kt`:

```kotlin
package com.trailmate.app.core.model

data class GearCatalogItem(
    val catalogItemId: String,
    val category: String,
    val brand: String,
    val model: String,
    val displayName: String,
    val weightGrams: Int?,
    val tags: List<String>,
    val source: String
)

data class GearCatalogInventoryItem(
    val inventoryItemId: String?,
    val catalogItemId: String?,
    val category: String,
    val brand: String?,
    val model: String,
    val displayName: String,
    val weightGrams: Int?,
    val available: Boolean,
    val custom: Boolean
)
```

Create `GearCatalogSelectionEngine.kt`:

```kotlin
package com.trailmate.app.core.model

object GearCatalogSelectionEngine {
    fun toInventoryItem(item: GearCatalogItem): GearCatalogInventoryItem =
        GearCatalogInventoryItem(
            inventoryItemId = null,
            catalogItemId = item.catalogItemId,
            category = item.category,
            brand = item.brand,
            model = item.model,
            displayName = item.displayName,
            weightGrams = item.weightGrams,
            available = true,
            custom = false
        )

    fun coversCategory(item: GearCatalogInventoryItem, recommendationCategory: String): Boolean =
        item.available && item.category.trim() == recommendationCategory.trim()
}
```

- [ ] **Step 8: Run matching test to verify green**

Run the matching test command. Expected: `BUILD SUCCESSFUL`.

---

## Task 4: Android Catalog Search UI State

**Files:**
- Create: `android-app/src/main/java/com/trailmate/app/feature/gear/GearCatalogSearchUiState.kt`
- Create: `android-app/src/test/java/com/trailmate/app/feature/gear/GearCatalogSearchUiStateTest.kt`

- [ ] **Step 1: Write failing UI state test**

Create `GearCatalogSearchUiStateTest.kt`:

```kotlin
package com.trailmate.app.feature.gear

import com.trailmate.app.core.model.GearCatalogItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GearCatalogSearchUiStateTest {
    @Test
    fun initialStateUsesRequestedCategory() {
        val state = GearCatalogSearchUiState.initial("头灯")

        assertEquals("头灯", state.category)
        assertEquals("", state.query)
        assertFalse(state.isLoading)
        assertTrue(state.results.isEmpty())
    }

    @Test
    fun loadingStateKeepsSearchInput() {
        val state = GearCatalogSearchUiState.initial("雨衣（防水透气）")
            .withQuery("beta")
            .loading()

        assertEquals("beta", state.query)
        assertTrue(state.isLoading)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun successStateStoresResults() {
        val item = GearCatalogItem(
            catalogItemId = "cat_rain_arcteryx_beta_lt",
            category = "雨衣（防水透气）",
            brand = "Arc'teryx",
            model = "Beta LT Jacket",
            displayName = "Arc'teryx Beta LT Jacket",
            weightGrams = 395,
            tags = emptyList(),
            source = "seed"
        )

        val state = GearCatalogSearchUiState.initial("雨衣（防水透气）")
            .success(listOf(item))

        assertFalse(state.isLoading)
        assertEquals(1, state.results.size)
        assertEquals("Arc'teryx Beta LT Jacket", state.results.first().displayName)
    }

    @Test
    fun failureStateShowsChineseMessage() {
        val state = GearCatalogSearchUiState.initial("头灯")
            .failure("装备库暂时不可用，请稍后重试。")

        assertFalse(state.isLoading)
        assertEquals("装备库暂时不可用，请稍后重试。", state.errorMessage)
    }
}
```

- [ ] **Step 2: Run UI state test to verify red**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.feature.gear.GearCatalogSearchUiStateTest" --console=plain
```

Expected: compilation fails because `GearCatalogSearchUiState` does not exist.

- [ ] **Step 3: Implement UI state**

Create `GearCatalogSearchUiState.kt`:

```kotlin
package com.trailmate.app.feature.gear

import com.trailmate.app.core.model.GearCatalogItem

data class GearCatalogSearchUiState(
    val category: String,
    val query: String,
    val isLoading: Boolean,
    val results: List<GearCatalogItem>,
    val errorMessage: String?
) {
    fun withQuery(nextQuery: String): GearCatalogSearchUiState =
        copy(query = nextQuery)

    fun loading(): GearCatalogSearchUiState =
        copy(isLoading = true, errorMessage = null)

    fun success(items: List<GearCatalogItem>): GearCatalogSearchUiState =
        copy(isLoading = false, results = items, errorMessage = null)

    fun failure(message: String): GearCatalogSearchUiState =
        copy(isLoading = false, errorMessage = message)

    companion object {
        fun initial(category: String): GearCatalogSearchUiState =
            GearCatalogSearchUiState(
                category = category,
                query = "",
                isLoading = false,
                results = emptyList(),
                errorMessage = null
            )
    }
}
```

- [ ] **Step 4: Run UI state test to verify green**

Run the same test command. Expected: `BUILD SUCCESSFUL`.

---

## Task 5: Android Gear UI Copy And Catalog Selection Surface

**Files:**
- Modify: `android-app/src/main/java/com/trailmate/app/feature/gear/MyGearScreen.kt`
- Modify: `android-app/src/main/java/com/trailmate/app/feature/route/detail/RouteGearTab.kt`
- Update: `android-app/src/androidTest/java/com/trailmate/app/TrailMateAppSmokeTest.kt`

- [ ] **Step 1: Write failing smoke assertion**

Update `TrailMateAppSmokeTest.kt` to assert the route gear action contains `选择装备` and does not present free-form entry as the primary missing-item action. Add a test method:

```kotlin
@Test
fun gearMissingItemUsesCatalogSelectionCopy() {
    composeRule.setContent {
        TrailMateApp()
    }

    composeRule.onNodeWithText("装备").performClick()
    composeRule.onNodeWithText("路线清单").assertExists()
    composeRule.onAllNodesWithText("选择装备").onFirst().assertExists()
}
```

If the test fixture needs an imported route first, use the existing sample-route helper already used in this smoke test file. Do not add broad setup beyond what is needed to reach the gear screen.

- [ ] **Step 2: Run smoke test to verify red**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat :android-app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.trailmate.app.TrailMateAppSmokeTest --console=plain
```

Expected: the new assertion fails because UI still says `添加已有装备` or the catalog selection surface does not exist.

- [ ] **Step 3: Replace missing item primary copy**

In `MyGearScreen.kt`, change missing item action text from:

```kotlin
text = "添加已有装备"
```

to:

```kotlin
text = "选择装备"
```

In `RouteGearTab.kt`, change:

```kotlin
Text("添加已有装备")
```

to:

```kotlin
Text("选择装备")
```

- [ ] **Step 4: Rename bottom sheet content**

In `MyGearScreen.kt`, replace `AddBrandGearPanel` title/copy:

```kotlin
text = "添加已有装备"
text = "快速从我的装备中选择"
Text("保存到我的装备")
```

with:

```kotlin
text = "选择装备"
text = "从 TrailMate 装备库搜索品牌和型号"
Text("加入我的装备")
```

Keep existing text fields temporarily as custom fallback if full catalog client integration is not yet wired in this task. Add a small caption near the fields:

```kotlin
Text(
    text = "找不到型号时可提交为自定义装备。",
    style = MaterialTheme.typography.bodySmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant
)
```

- [ ] **Step 5: Run focused unit/UI tests**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.feature.gear.GearCatalogSearchUiStateTest" --console=plain
```

Expected: `BUILD SUCCESSFUL`.

Run connected smoke if device is available:

```powershell
.\.android-sdk\platform-tools\adb.exe devices -l
.\gradlew.bat :android-app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.trailmate.app.TrailMateAppSmokeTest --console=plain
```

Expected: the new smoke assertion passes or reports only unrelated known environment failures. Investigate any UI-copy failure.

---

## Task 6: Full-Screen Navigation Surface Policy

**Files:**
- Create: `android-app/src/main/java/com/trailmate/app/core/model/FullscreenNavigationSurfacePolicy.kt`
- Create: `android-app/src/test/java/com/trailmate/app/core/model/FullscreenNavigationSurfacePolicyTest.kt`
- Modify: `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`

- [ ] **Step 1: Write failing policy test**

Create `FullscreenNavigationSurfacePolicyTest.kt`:

```kotlin
package com.trailmate.app.core.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FullscreenNavigationSurfacePolicyTest {
    @Test
    fun firstViewportAllowsOnlyFieldCriticalSurfaces() {
        assertTrue(FullscreenNavigationSurfacePolicy.isAllowedInFirstViewport("map"))
        assertTrue(FullscreenNavigationSurfacePolicy.isAllowedInFirstViewport("current_location"))
        assertTrue(FullscreenNavigationSurfacePolicy.isAllowedInFirstViewport("recording_action"))
        assertTrue(FullscreenNavigationSurfacePolicy.isAllowedInFirstViewport("safety_share"))

        assertFalse(FullscreenNavigationSurfacePolicy.isAllowedInFirstViewport("amap_launch_diagnostics"))
        assertFalse(FullscreenNavigationSurfacePolicy.isAllowedInFirstViewport("baseline_profile_evidence"))
        assertFalse(FullscreenNavigationSurfacePolicy.isAllowedInFirstViewport("ai_gear_explanation"))
        assertFalse(FullscreenNavigationSurfacePolicy.isAllowedInFirstViewport("offline_map_long_explanation"))
    }
}
```

- [ ] **Step 2: Run policy test to verify red**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.model.FullscreenNavigationSurfacePolicyTest" --console=plain
```

Expected: compilation fails because policy does not exist.

- [ ] **Step 3: Implement policy**

Create `FullscreenNavigationSurfacePolicy.kt`:

```kotlin
package com.trailmate.app.core.model

object FullscreenNavigationSurfacePolicy {
    private val allowedFirstViewportSurfaces = setOf(
        "map",
        "current_location",
        "route_polyline",
        "checkpoints",
        "recorded_track",
        "current_checkpoint",
        "next_checkpoint",
        "gps_status",
        "recording_status",
        "base_map_status",
        "gear_status",
        "recording_action",
        "safety_share"
    )

    fun isAllowedInFirstViewport(surfaceId: String): Boolean =
        surfaceId in allowedFirstViewportSurfaces
}
```

- [ ] **Step 4: Run policy test to verify green**

Run the same command. Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Apply UI rule in `RouteDetailScreen.kt`**

Inspect `FullscreenRouteNavigationContent` and remove or move first-viewport direct diagnostic components. Preserve:

- route map;
- current checkpoint card;
- track progress;
- `定位：可靠` or related GPS status;
- `轨迹：N 点`;
- base map compact status;
- download offline map button only if compact and not long explanation;
- safety share;
- recording controls.

Move long explanations or diagnostic text behind an existing settings/detail action. Do not delete the diagnostic engines.

- [ ] **Step 6: Verify focused tests**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat :android-app:testDebugUnitTest --tests "com.trailmate.app.core.model.FullscreenNavigationSurfacePolicyTest" --console=plain
```

Expected: `BUILD SUCCESSFUL`.

---

## Task 7: API Documentation And OpenSpec Progress

**Files:**
- Modify: `docs/api/trailmate-server-api.md`
- Modify: `docs/deployment/ubuntu-docker-compose.md`
- Modify: `openspec/changes/trailmate-production-readiness-v2/tasks.md`

- [ ] **Step 1: Update server API docs**

In `docs/api/trailmate-server-api.md`, add endpoint rows:

```markdown
| `GET` | `/gear/catalog/categories` | `listGearCatalogCategories` | Server-owned gear categories for route matching |
| `GET` | `/gear/catalog/search` | `searchGearCatalog` | Searches server-owned gear catalog by category and query |
| `GET` | `/gear/inventory` | `listGearInventory` | User-owned gear inventory |
| `POST` | `/gear/inventory` | `saveGearInventoryItem` | Saves a selected catalog item or custom fallback |
| `PATCH` | `/gear/inventory/{inventoryItemId}` | `updateGearInventoryItem` | Updates availability |
| `DELETE` | `/gear/inventory/{inventoryItemId}` | `deleteGearInventoryItem` | Removes ownership record |
```

Add a `### Gear Catalog` section:

```markdown
### Gear Catalog

Catalog search:

```json
[
  {
    "catalogItemId": "cat_rain_arcteryx_beta_lt",
    "category": "雨衣（防水透气）",
    "brand": "Arc'teryx",
    "model": "Beta LT Jacket",
    "displayName": "Arc'teryx Beta LT Jacket",
    "weightGrams": 395,
    "tags": ["防水", "硬壳", "暴露路段"],
    "source": "seed"
  }
]
```

Inventory create:

```json
{
  "catalogItemId": "cat_rain_arcteryx_beta_lt",
  "available": true
}
```

Custom fallback:

```json
{
  "catalogItemId": null,
  "category": "保温层（抓绒/羽绒）",
  "brand": "Montbell",
  "model": "自定义羽绒服",
  "available": true
}
```

Custom fallback items must be labeled as custom in Android.
```

- [ ] **Step 2: Update deployment docs**

In `docs/deployment/ubuntu-docker-compose.md`, add catalog smoke check:

```bash
curl -s http://127.0.0.1:8080/api/v1/gear/catalog/search?q=beta
curl -s http://127.0.0.1:8080/api/v1/gear/catalog/categories
```

Expected response includes `cat_rain_arcteryx_beta_lt` and categories such as `头灯`.

- [ ] **Step 3: Update OpenSpec tasks**

Mark completed tasks in `openspec/changes/trailmate-production-readiness-v2/tasks.md` as implementation progresses. Do not mark deployment or true-device validation complete until commands have run successfully.

---

## Task 8: Full Verification, Remote Server, And True Device

**Files:**
- No source file edits expected.
- Output artifacts under `outputs/qa-artifacts/2026-06-23/`.

- [ ] **Step 1: Run server and Android unit tests**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat :trailmate-server:test :android-app:testDebugUnitTest :android-app:assembleDebug --console=plain
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 2: Check Lovable project state**

Run Lovable status check for project `9a77add1-9bfc-4574-a8c4-bd67bfbfb196`.

Expected: status is `ready` or an actionable failure is documented. Lovable is not a blocker for Android implementation if local design and OpenSpec are complete.

- [ ] **Step 3: Deploy/update remote-linux**

Use:

```powershell
ssh remote-linux "cd /opt/TrailMate && docker compose ps"
```

If server code changed, copy or pull the updated repository using the existing deployment workflow documented in `docs/deployment/ubuntu-docker-compose.md`, rebuild the server image, and run:

```bash
cd /opt/TrailMate
docker compose up -d --build
docker compose logs --tail=120 trailmate-server
curl -s http://127.0.0.1:8080/actuator/health
curl -s "http://127.0.0.1:8080/api/v1/gear/catalog/search?q=beta"
```

Expected: health is `UP`, catalog response includes `cat_rain_arcteryx_beta_lt`.

- [ ] **Step 4: Install APK on SM_S9260**

Run:

```powershell
.\.android-sdk\platform-tools\adb.exe devices -l
.\.android-sdk\platform-tools\adb.exe -s R5CX12KKJNJ shell input keyevent 224
.\.android-sdk\platform-tools\adb.exe -s R5CX12KKJNJ install -r android-app\build\outputs\apk\debug\android-app-debug.apk
.\.android-sdk\platform-tools\adb.exe -s R5CX12KKJNJ shell am force-stop com.trailmate.app
.\.android-sdk\platform-tools\adb.exe -s R5CX12KKJNJ shell am start -n com.trailmate.app/.MainActivity
```

Use password `1027` only if the device is locked and adb input requires unlock.

- [ ] **Step 5: True-device UI verification**

Use UIAutomator dumps and screenshots:

```powershell
.\.android-sdk\platform-tools\adb.exe -s R5CX12KKJNJ exec-out uiautomator dump /dev/tty > outputs\qa-artifacts\2026-06-23\tm-production-v2-ui.xml
Get-Content outputs\qa-artifacts\2026-06-23\tm-production-v2-ui.xml | Select-String -Pattern '微信登录|准备走哪条线|选择装备|全屏导航|定位：可靠|轨迹：'
```

Expected evidence:

- login/onboarding copy is readable;
- route import path still exists;
- gear missing item action says `选择装备`;
- full-screen navigation does not show diagnostics in the first viewport;
- GPS/track status can reach reliable/recording states when location is available.

---

## Self-Review

- Spec coverage:
  - `auth-onboarding`: covered by login/onboarding copy tasks and tests.
  - `server-gear-catalog`: covered by server service/controller tests and docs.
  - `mobile-product-ux`: covered by gear copy/UI state, fullscreen policy, and true-device checks.
- Placeholder scan:
  - No `TBD`, `TODO`, or vague “add tests” steps remain.
- Type consistency:
  - Server names use `GearCatalogItem`, `GearInventoryItem`, `GearService`.
  - Android DTO names use `TrailMateGearCatalogItemDto` and model names use `GearCatalogItem`.
  - Endpoint paths match OpenSpec and API docs.
