package com.trailmate.server.map;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OfflineBasemapService {
    private static final String LOCAL_PMTILES_DOWNLOAD_PREFIX = "/offline-basemaps/pmtiles/";

    private final OfflineBasemapCatalogRepository catalogRepository;
    private final OfflineBasemapFileService fileService;
    private final Map<Path, LocalPmTilesMetadata> metadataCache = new ConcurrentHashMap<>();

    public OfflineBasemapService(OfflineBasemapCatalogRepository catalogRepository) {
        this(catalogRepository, null);
    }

    public OfflineBasemapService(
        OfflineBasemapCatalogRepository catalogRepository,
        OfflineBasemapFileService fileService
    ) {
        this.catalogRepository = catalogRepository;
        this.fileService = fileService;
    }

    public List<OfflineBasemapCatalogItem> listPmTilesCatalog(
        double minLongitude,
        double minLatitude,
        double maxLongitude,
        double maxLatitude
    ) {
        OfflineBasemapBounds routeBounds = new OfflineBasemapBounds(
            minLongitude,
            minLatitude,
            maxLongitude,
            maxLatitude
        );
        return catalogRepository.listPmTilesPacks().stream()
            .filter(pack -> pack.contains(routeBounds))
            .map(this::withLocalPmTilesMetadata)
            .toList();
    }

    private OfflineBasemapCatalogItem withLocalPmTilesMetadata(OfflineBasemapCatalogItem pack) {
        if (fileService == null || pack.downloadUrl() == null) {
            return pack;
        }
        if (!pack.downloadUrl().startsWith(LOCAL_PMTILES_DOWNLOAD_PREFIX)) {
            return pack;
        }

        String fileName = pack.downloadUrl().substring(LOCAL_PMTILES_DOWNLOAD_PREFIX.length());
        Path path = fileService.findPmTilesFile(fileName).orElse(null);
        if (path == null) {
            return pack;
        }
        try {
            return copyWithFileMetadata(pack, localMetadata(path));
        } catch (IOException ignored) {
            return pack;
        }
    }

    private OfflineBasemapCatalogItem copyWithFileMetadata(
        OfflineBasemapCatalogItem pack,
        LocalPmTilesMetadata metadata
    ) {
        return new OfflineBasemapCatalogItem(
            pack.packId(),
            pack.regionName(),
            pack.downloadUrl(),
            metadata.sizeBytes(),
            metadata.sha256(),
            pack.tileType(),
            pack.minZoom(),
            pack.maxZoom(),
            pack.minLongitude(),
            pack.minLatitude(),
            pack.maxLongitude(),
            pack.maxLatitude(),
            pack.attribution(),
            pack.source()
        );
    }

    private LocalPmTilesMetadata localMetadata(Path path) throws IOException {
        Path cacheKey = path.toAbsolutePath().normalize();
        long sizeBytes = Files.size(path);
        FileTime lastModifiedTime = Files.getLastModifiedTime(path);
        LocalPmTilesMetadata cached = metadataCache.get(cacheKey);
        if (cached != null
            && cached.sizeBytes() == sizeBytes
            && cached.lastModifiedTime().equals(lastModifiedTime)) {
            return cached;
        }

        LocalPmTilesMetadata metadata = new LocalPmTilesMetadata(
            sizeBytes,
            lastModifiedTime,
            sha256(path)
        );
        metadataCache.put(cacheKey, metadata);
        return metadata;
    }

    private static String sha256(Path path) throws IOException {
        MessageDigest digest = newSha256Digest();
        byte[] buffer = new byte[8192];
        try (InputStream input = Files.newInputStream(path)) {
            int read;
            while ((read = input.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        return hex(digest.digest());
    }

    private static MessageDigest newSha256Digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 digest is not available", exception);
        }
    }

    private static String hex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            builder.append(String.format("%02x", value));
        }
        return builder.toString();
    }

    private record LocalPmTilesMetadata(long sizeBytes, FileTime lastModifiedTime, String sha256) {
    }
}
