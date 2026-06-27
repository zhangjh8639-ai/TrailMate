package com.trailmate.server.map;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OfflineBasemapServiceTest {
    @TempDir
    Path pmTilesDirectory;

    @Test
    void enrichesLocalPmTilesCatalogItemWithFileSizeAndSha256() throws IOException {
        Files.write(pmTilesDirectory.resolve("hangzhou-westlake.pmtiles"), new byte[] {1, 2, 3, 4});
        OfflineBasemapService service = new OfflineBasemapService(
            () -> List.of(pack("/offline-basemaps/pmtiles/hangzhou-westlake.pmtiles", 120_000_000L, null)),
            new OfflineBasemapFileService(pmTilesDirectory)
        );

        OfflineBasemapCatalogItem item = service.listPmTilesCatalog(120.01, 30.06, 120.29, 30.39).get(0);

        assertEquals(4L, item.sizeBytes());
        assertEquals("9f64a747e1b97f131fabb6b447296c9b6f0201e79fb3c5356e6c77e89b6a806a", item.sha256());
    }

    @Test
    void keepsConfiguredMetadataWhenLocalPmTilesFileIsMissing() {
        OfflineBasemapService service = new OfflineBasemapService(
            () -> List.of(pack("/offline-basemaps/pmtiles/hangzhou-westlake.pmtiles", 123L, "configured")),
            new OfflineBasemapFileService(pmTilesDirectory)
        );

        OfflineBasemapCatalogItem item = service.listPmTilesCatalog(120.01, 30.06, 120.29, 30.39).get(0);

        assertEquals(123L, item.sizeBytes());
        assertEquals("configured", item.sha256());
    }

    @Test
    void keepsConfiguredMetadataForExternalPmTilesUrls() {
        OfflineBasemapService service = new OfflineBasemapService(
            () -> List.of(pack("https://cdn.example.com/hangzhou-westlake.pmtiles", 123L, "configured")),
            new OfflineBasemapFileService(pmTilesDirectory)
        );

        OfflineBasemapCatalogItem item = service.listPmTilesCatalog(120.01, 30.06, 120.29, 30.39).get(0);

        assertEquals(123L, item.sizeBytes());
        assertEquals("configured", item.sha256());
    }

    @Test
    void excludesPmTilesPackThatOnlyPartiallyIntersectsRequestedBounds() {
        OfflineBasemapService service = new OfflineBasemapService(
            () -> List.of(
                new OfflineBasemapCatalogItem(
                    "partial",
                    "Partial",
                    "/offline-basemaps/pmtiles/partial.pmtiles",
                    null,
                    null,
                    "MVT",
                    10,
                    14,
                    120.00,
                    30.05,
                    120.10,
                    30.40,
                    "OpenStreetMap contributors",
                    "OSM / Protomaps"
                )
            ),
            new OfflineBasemapFileService(pmTilesDirectory)
        );

        assertTrue(service.listPmTilesCatalog(120.05, 30.10, 120.25, 30.35).isEmpty());
    }

    @Test
    void reusesCachedChecksumWhenLocalPmTilesFileMetadataIsUnchanged() throws IOException {
        Path pmTiles = pmTilesDirectory.resolve("hangzhou-westlake.pmtiles");
        FileTime timestamp = FileTime.fromMillis(1_700_000_000_000L);
        Files.write(pmTiles, new byte[] {1, 2, 3, 4});
        Files.setLastModifiedTime(pmTiles, timestamp);
        OfflineBasemapService service = new OfflineBasemapService(
            () -> List.of(pack("/offline-basemaps/pmtiles/hangzhou-westlake.pmtiles", null, null)),
            new OfflineBasemapFileService(pmTilesDirectory)
        );

        service.listPmTilesCatalog(120.01, 30.06, 120.29, 30.39);
        Files.write(pmTiles, new byte[] {4, 3, 2, 1});
        Files.setLastModifiedTime(pmTiles, timestamp);

        OfflineBasemapCatalogItem item = service.listPmTilesCatalog(120.01, 30.06, 120.29, 30.39).get(0);

        assertEquals("9f64a747e1b97f131fabb6b447296c9b6f0201e79fb3c5356e6c77e89b6a806a", item.sha256());
    }

    @Test
    void refreshesCachedChecksumWhenLocalPmTilesFileTimestampChanges() throws IOException {
        Path pmTiles = pmTilesDirectory.resolve("hangzhou-westlake.pmtiles");
        Files.write(pmTiles, new byte[] {1, 2, 3, 4});
        Files.setLastModifiedTime(pmTiles, FileTime.fromMillis(1_700_000_000_000L));
        OfflineBasemapService service = new OfflineBasemapService(
            () -> List.of(pack("/offline-basemaps/pmtiles/hangzhou-westlake.pmtiles", null, null)),
            new OfflineBasemapFileService(pmTilesDirectory)
        );

        service.listPmTilesCatalog(120.01, 30.06, 120.29, 30.39);
        Files.write(pmTiles, new byte[] {4, 3, 2, 1});
        Files.setLastModifiedTime(pmTiles, FileTime.fromMillis(1_700_000_001_000L));

        OfflineBasemapCatalogItem item = service.listPmTilesCatalog(120.01, 30.06, 120.29, 30.39).get(0);

        assertEquals("ee10da4aefe61a37df1dee937ca3221afa3b2351f9ea34edbbb769573c6785f7", item.sha256());
    }

    private static OfflineBasemapCatalogItem pack(String downloadUrl, Long sizeBytes, String sha256) {
        return new OfflineBasemapCatalogItem(
            "pack",
            "Hangzhou",
            downloadUrl,
            sizeBytes,
            sha256,
            "MVT",
            10,
            14,
            120.00,
            30.05,
            120.30,
            30.40,
            "OpenStreetMap contributors",
            "OSM / Protomaps"
        );
    }
}
