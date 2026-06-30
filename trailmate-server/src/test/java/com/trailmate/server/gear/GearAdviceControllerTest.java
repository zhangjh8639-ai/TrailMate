package com.trailmate.server.gear;

import com.trailmate.server.auth.AuthAccessTokenVerifier;
import com.trailmate.server.auth.AuthenticatedUser;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GearAdviceControllerTest {
    private final InMemoryGearAdviceArtifactRepository artifactRepository =
        new InMemoryGearAdviceArtifactRepository();
    private final GearAdviceService service = new GearAdviceService(
        new InMemoryGearCatalogRepository(),
        artifactRepository
    );
    private final FakeAccessTokenVerifier accessTokenVerifier = new FakeAccessTokenVerifier();
    private final MockMvc mockMvc = MockMvcBuilders
        .standaloneSetup(new GearAdviceController(
            service,
            accessTokenVerifier,
            Clock.fixed(Instant.parse("2026-06-30T08:00:00Z"), ZoneOffset.UTC)
        ))
        .build();

    @Test
    void gearAdviceRequiresBearerToken() throws Exception {
        mockMvc.perform(post("/api/v1/plans/plan-123/gear-advice")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequestJson()))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("AUTH_REQUIRED"));
    }

    @Test
    void gearAdviceRejectsInvalidBearerToken() throws Exception {
        mockMvc.perform(post("/api/v1/plans/plan-123/gear-advice")
                .header("Authorization", "Bearer invalid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequestJson()))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("AUTH_REQUIRED"));
    }

    @Test
    void gearAdviceMatchesServerCatalogFromVerifiedTokenAndPersistsChecklistArtifact() throws Exception {
        mockMvc.perform(post("/api/v1/plans/plan-123/gear-advice")
                .header("Authorization", "Bearer access-token-123")
                .header("X-TrailMate-User-Id", "spoofed-user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequestJson()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.assessmentFingerprint").value("fp-longjing-1"))
            .andExpect(jsonPath("$.recommendations[0].category").value("头灯"))
            .andExpect(jsonPath("$.recommendations[0].matchedGearItemId").value("cat_headlamp_bd_spot_400"));

        Optional<GearAdviceArtifact> saved = artifactRepository.latestFor(
            "usr-1",
            "plan-123",
            "fp-longjing-1"
        );
        assertTrue(saved.isPresent());
        assertEquals("cat_headlamp_bd_spot_400", saved.get().recommendations().get(0).matchedGearItemId());
    }

    @Test
    void gearAdviceIgnoresClientSuppliedCatalogMatchIds() throws Exception {
        mockMvc.perform(post("/api/v1/plans/plan-123/gear-advice")
                .header("Authorization", "Bearer access-token-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequestJsonWithBogusClientMatch()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.recommendations[0].matchedGearItemId").value("cat_headlamp_bd_spot_400"));
    }

    private String validRequestJson() {
        return """
            {
              "assessmentFingerprint": "fp-longjing-1",
              "fallbackRecommendations": [
                {
                  "category": "头灯",
                  "status": "CHECK",
                  "rationale": "预计耗时较长，确认电量。",
                  "matchedGearItemId": null
                },
                {
                  "category": "登山杖",
                  "status": "MISSING",
                  "rationale": "累计爬升较高，下坡段需要稳定支撑。"
                }
              ],
              "guardrails": [
                "不要改写路线评估",
                "候选由服务端品牌库匹配"
              ]
            }
            """;
    }

    private String validRequestJsonWithBogusClientMatch() {
        return """
            {
              "assessmentFingerprint": "fp-longjing-2",
              "fallbackRecommendations": [
                {
                  "category": "头灯",
                  "status": "CHECK",
                  "rationale": "预计耗时较长，确认电量。",
                  "matchedGearItemId": "cat_nonexistent_or_wrong_category"
                }
              ],
              "guardrails": [
                "不要改写路线评估",
                "候选由服务端品牌库匹配"
              ]
            }
            """;
    }

    private static final class FakeAccessTokenVerifier implements AuthAccessTokenVerifier {
        @Override
        public AuthenticatedUser verifyAccessToken(String accessToken, Instant now) {
            if (!"access-token-123".equals(accessToken)) {
                throw new IllegalArgumentException("Invalid access token.");
            }
            return new AuthenticatedUser("usr-1");
        }
    }
}
