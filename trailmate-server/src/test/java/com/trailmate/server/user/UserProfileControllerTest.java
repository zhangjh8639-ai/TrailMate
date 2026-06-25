package com.trailmate.server.user;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserProfileControllerTest {
    private final MockMvc mockMvc = MockMvcBuilders
        .standaloneSetup(new UserProfileController(new UserProfileService(new InMemoryUserProfileRepository())))
        .build();

    @Test
    void putProfileStoresBaselineEvidenceForCurrentUser() throws Exception {
        mockMvc.perform(put("/api/v1/users/me/profile")
                .header("X-TrailMate-User-Id", "usr_123")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "exerciseFrequency": "ONE_TO_TWO_PER_WEEK",
                      "typicalDuration": "OVER_60",
                      "experienceLevel": "REGULAR",
                      "ascentExperience": "M300_TO_800",
                      "heightCm": 178,
                      "weightKg": 70,
                      "commonPackWeightKg": 6
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value("usr_123"))
            .andExpect(jsonPath("$.exerciseFrequency").value("ONE_TO_TWO_PER_WEEK"))
            .andExpect(jsonPath("$.heightCm").value(178));
    }

    @Test
    void getProfileReturnsSavedBaselineEvidence() throws Exception {
        mockMvc.perform(put("/api/v1/users/me/profile")
                .header("X-TrailMate-User-Id", "usr_123")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "exerciseFrequency": "THREE_PLUS_PER_WEEK",
                      "typicalDuration": "OVER_60",
                      "experienceLevel": "EXPERIENCED",
                      "ascentExperience": "OVER_800",
                      "heightCm": 180,
                      "weightKg": 72,
                      "commonPackWeightKg": 8
                    }
                    """))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/users/me/profile")
                .header("X-TrailMate-User-Id", "usr_123"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value("usr_123"))
            .andExpect(jsonPath("$.experienceLevel").value("EXPERIENCED"))
            .andExpect(jsonPath("$.commonPackWeightKg").value(8));
    }

    @Test
    void getProfileReturnsNotFoundWhenUserHasNoProfile() throws Exception {
        mockMvc.perform(get("/api/v1/users/me/profile")
                .header("X-TrailMate-User-Id", "usr_missing"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("USER_PROFILE_NOT_FOUND"));
    }

    @Test
    void profileEndpointFallsBackToPreviewUserForSmokeTests() throws Exception {
        mockMvc.perform(put("/api/v1/users/me/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "exerciseFrequency": "RARELY",
                      "typicalDuration": "UNDER_30",
                      "experienceLevel": "BEGINNER",
                      "ascentExperience": "UNDER_300",
                      "heightCm": null,
                      "weightKg": null,
                      "commonPackWeightKg": null
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value("local-preview-user"));
    }
}
