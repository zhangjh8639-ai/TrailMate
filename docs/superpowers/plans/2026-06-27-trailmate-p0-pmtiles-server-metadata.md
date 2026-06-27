# TrailMate P0 PMTiles Server Metadata Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Publish trustworthy PMTiles file size and SHA-256 metadata from the server catalog whenever the server hosts the local PMTiles file.

**Architecture:** Keep `OfflineBasemapService` as the catalog query boundary and add optional enrichment with `OfflineBasemapFileService`. Only server-local `/offline-basemaps/pmtiles/*.pmtiles` URLs are enriched; missing files and external URLs keep configured metadata.

**Tech Stack:** Java 17, Spring Boot server module, JUnit 5, OpenSpec.

---

### Task 1: Service Metadata Enrichment

**Files:**
- Create: `trailmate-server/src/test/java/com/trailmate/server/map/OfflineBasemapServiceTest.java`
- Modify: `trailmate-server/src/main/java/com/trailmate/server/map/OfflineBasemapService.java`
- Modify: `trailmate-server/src/main/java/com/trailmate/server/map/OfflineBasemapProviderConfiguration.java`

- [ ] **Step 1: Write the failing tests**

Create `OfflineBasemapServiceTest` with three tests:

```java
@Test
void enrichesLocalPmTilesCatalogItemWithFileSizeAndSha256() throws IOException {
    Path pmTiles = tempDir.resolve("hangzhou-westlake.pmtiles");
    Files.write(pmTiles, new byte[] {1, 2, 3, 4});

    OfflineBasemapService service = new OfflineBasemapService(
        () -> List.of(pack("/offline-basemaps/pmtiles/hangzhou-westlake.pmtiles", 120_000_000L, null)),
        new OfflineBasemapFileService(tempDir)
    );

    OfflineBasemapCatalogItem item = service.listPmTilesCatalog(120.01, 30.06, 120.29, 30.39).get(0);

    assertThat(item.sizeBytes()).isEqualTo(4L);
    assertThat(item.sha256()).isEqualTo("9f64a747e1b97f131fabb6b447296c9b6f0201e79fb3c5356e6c77e89b6a806a");
}

@Test
void keepsConfiguredMetadataWhenLocalPmTilesFileIsMissing() {
    OfflineBasemapService service = new OfflineBasemapService(
        () -> List.of(pack("/offline-basemaps/pmtiles/hangzhou-westlake.pmtiles", 123L, "configured")),
        new OfflineBasemapFileService(tempDir)
    );

    OfflineBasemapCatalogItem item = service.listPmTilesCatalog(120.01, 30.06, 120.29, 30.39).get(0);

    assertThat(item.sizeBytes()).isEqualTo(123L);
    assertThat(item.sha256()).isEqualTo("configured");
}

@Test
void keepsConfiguredMetadataForExternalPmTilesUrls() {
    OfflineBasemapService service = new OfflineBasemapService(
        () -> List.of(pack("https://cdn.example.com/hangzhou-westlake.pmtiles", 123L, "configured")),
        new OfflineBasemapFileService(tempDir)
    );

    OfflineBasemapCatalogItem item = service.listPmTilesCatalog(120.01, 30.06, 120.29, 30.39).get(0);

    assertThat(item.sizeBytes()).isEqualTo(123L);
    assertThat(item.sha256()).isEqualTo("configured");
}
```

- [ ] **Step 2: Run tests to verify RED**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'; $env:Path="$env:JAVA_HOME\bin;$env:Path"; .\gradlew.bat :trailmate-server:test --tests "com.trailmate.server.map.OfflineBasemapServiceTest" --no-daemon --console=plain
```

Expected: compilation fails because `OfflineBasemapService` does not yet accept `OfflineBasemapFileService`.

- [ ] **Step 3: Implement minimal service enrichment**

Add a two-argument `OfflineBasemapService` constructor, keep the current one-argument constructor, and map filtered packs through an enrichment helper. The helper should:

- check the local URL prefix `/offline-basemaps/pmtiles/`;
- extract the file name;
- use `OfflineBasemapFileService.findPmTilesFile(fileName)`;
- return original metadata when no file exists or an `IOException` occurs;
- compute SHA-256 with a streaming `MessageDigest` when the file exists.

- [ ] **Step 4: Wire production configuration**

Change `OfflineBasemapProviderConfiguration.offlineBasemapService(...)` to accept `OfflineBasemapFileService` and construct `new OfflineBasemapService(offlineBasemapCatalogRepository, offlineBasemapFileService)`.

- [ ] **Step 5: Verify GREEN**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'; $env:Path="$env:JAVA_HOME\bin;$env:Path"; .\gradlew.bat :trailmate-server:test --tests "com.trailmate.server.map.OfflineBasemapServiceTest" --no-daemon --console=plain
```

Expected: targeted server test passes.

- [ ] **Step 6: Full verification**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'; $env:Path="$env:JAVA_HOME\bin;$env:Path"; .\gradlew.bat :android-app:testDebugUnitTest :trailmate-server:test --no-daemon --console=plain
openspec validate --all --strict
git diff --check
```

Expected: all commands pass.
