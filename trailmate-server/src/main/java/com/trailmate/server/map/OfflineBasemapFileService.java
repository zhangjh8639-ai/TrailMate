package com.trailmate.server.map;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class OfflineBasemapFileService {
    private final Path pmTilesDirectory;

    public OfflineBasemapFileService(Path pmTilesDirectory) {
        this.pmTilesDirectory = pmTilesDirectory.toAbsolutePath().normalize();
    }

    public Optional<Path> findPmTilesFile(String fileName) {
        if (fileName == null || !fileName.endsWith(".pmtiles") || fileName.contains("/") || fileName.contains("\\")) {
            return Optional.empty();
        }
        Path resolved = pmTilesDirectory.resolve(fileName).normalize();
        if (!resolved.startsWith(pmTilesDirectory) || !Files.isRegularFile(resolved)) {
            return Optional.empty();
        }
        return Optional.of(resolved);
    }
}
