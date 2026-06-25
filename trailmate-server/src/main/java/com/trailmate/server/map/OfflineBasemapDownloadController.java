package com.trailmate.server.map;

import java.nio.file.Path;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/offline-basemaps/pmtiles")
public class OfflineBasemapDownloadController {
    private final OfflineBasemapFileService fileService;

    public OfflineBasemapDownloadController(OfflineBasemapFileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/{fileName:.+}")
    public ResponseEntity<FileSystemResource> downloadPmTiles(@PathVariable String fileName) {
        return fileService.findPmTilesFile(fileName)
            .map(this::okResource)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private ResponseEntity<FileSystemResource> okResource(Path file) {
        FileSystemResource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .contentLength(file.toFile().length())
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
            .body(resource);
    }
}
