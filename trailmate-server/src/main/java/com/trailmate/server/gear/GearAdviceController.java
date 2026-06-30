package com.trailmate.server.gear;

import com.trailmate.server.auth.AuthAccessTokenVerifier;
import com.trailmate.server.auth.AuthenticatedUser;
import java.time.Clock;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/plans")
public class GearAdviceController {
    private final GearAdviceService gearAdviceService;
    private final AuthAccessTokenVerifier accessTokenVerifier;
    private final Clock clock;

    public GearAdviceController(
        GearAdviceService gearAdviceService,
        AuthAccessTokenVerifier accessTokenVerifier,
        Clock clock
    ) {
        this.gearAdviceService = gearAdviceService;
        this.accessTokenVerifier = accessTokenVerifier;
        this.clock = clock;
    }

    @PostMapping("/{planId}/gear-advice")
    public GearAdviceResponse requestGearAdvice(
        @PathVariable String planId,
        @RequestHeader(name = "Authorization", required = false) String authorization,
        @RequestBody GearAdviceRequest request
    ) {
        AuthenticatedUser user = verifyBearerToken(authorization);
        return gearAdviceService.advise(user.userId(), planId, request);
    }

    @ExceptionHandler(GearAdviceAuthRequiredException.class)
    public ResponseEntity<GearApiErrorResponse> handleAuthRequired(GearAdviceAuthRequiredException exception) {
        return ResponseEntity
            .status(401)
            .body(new GearApiErrorResponse(
                401,
                "AUTH_REQUIRED",
                exception.getMessage(),
                UUID.randomUUID().toString()
            ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GearApiErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity
            .badRequest()
            .body(new GearApiErrorResponse(
                400,
                "GEAR_ADVICE_INVALID_REQUEST",
                exception.getMessage() == null ? "Invalid gear advice request." : exception.getMessage(),
                UUID.randomUUID().toString()
            ));
    }

    private void requireBearerToken(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            throw new GearAdviceAuthRequiredException();
        }
        if (!authorization.trim().startsWith("Bearer ")) {
            throw new GearAdviceAuthRequiredException();
        }
    }

    private AuthenticatedUser verifyBearerToken(String authorization) {
        requireBearerToken(authorization);
        String accessToken = authorization.trim().substring("Bearer ".length()).trim();
        if (accessToken.isBlank()) {
            throw new GearAdviceAuthRequiredException();
        }
        try {
            return accessTokenVerifier.verifyAccessToken(accessToken, clock.instant());
        } catch (IllegalArgumentException exception) {
            throw new GearAdviceAuthRequiredException();
        }
    }
}
