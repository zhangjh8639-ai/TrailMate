package com.trailmate.server.map;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OfflineBasemapDownloadControllerTest {
    @TempDir
    Path pmTilesDirectory;

    @Test
    void servesPmTilesFileFromConfiguredDirectory() throws Exception {
        Files.write(pmTilesDirectory.resolve("hangzhou-westlake.pmtiles"), new byte[] {1, 2, 3, 4});
        MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new OfflineBasemapDownloadController(new OfflineBasemapFileService(pmTilesDirectory)))
            .build();

        mockMvc.perform(get("/offline-basemaps/pmtiles/hangzhou-westlake.pmtiles"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "application/octet-stream"))
            .andExpect(header().longValue("Content-Length", 4L))
            .andExpect(content().bytes(new byte[] {1, 2, 3, 4}));
    }

    @Test
    void returnsNotFoundForMissingOrUnsafePath() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new OfflineBasemapDownloadController(new OfflineBasemapFileService(pmTilesDirectory)))
            .build();

        mockMvc.perform(get("/offline-basemaps/pmtiles/missing.pmtiles"))
            .andExpect(status().isNotFound());

        mockMvc.perform(get("/offline-basemaps/pmtiles/../application.yml"))
            .andExpect(status().isNotFound());
    }

    @Test
    void servesSatisfiablePmTilesByteRange() throws Exception {
        Files.write(pmTilesDirectory.resolve("hangzhou-westlake.pmtiles"), new byte[] {1, 2, 3, 4});
        MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new OfflineBasemapDownloadController(new OfflineBasemapFileService(pmTilesDirectory)))
            .build();

        mockMvc.perform(get("/offline-basemaps/pmtiles/hangzhou-westlake.pmtiles")
                .header("Range", "bytes=2-"))
            .andExpect(status().isPartialContent())
            .andExpect(header().string("Accept-Ranges", "bytes"))
            .andExpect(header().string("Content-Range", "bytes 2-3/4"))
            .andExpect(header().longValue("Content-Length", 2L))
            .andExpect(content().bytes(new byte[] {3, 4}));
    }

    @Test
    void rejectsUnsatisfiablePmTilesByteRange() throws Exception {
        Files.write(pmTilesDirectory.resolve("hangzhou-westlake.pmtiles"), new byte[] {1, 2, 3, 4});
        MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new OfflineBasemapDownloadController(new OfflineBasemapFileService(pmTilesDirectory)))
            .build();

        mockMvc.perform(get("/offline-basemaps/pmtiles/hangzhou-westlake.pmtiles")
                .header("Range", "bytes=9-"))
            .andExpect(status().isRequestedRangeNotSatisfiable())
            .andExpect(header().string("Accept-Ranges", "bytes"))
            .andExpect(header().string("Content-Range", "bytes */4"));
    }
}
