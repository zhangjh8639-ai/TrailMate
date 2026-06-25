package com.trailmate.server.user;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me/profile")
public class UserProfileController {
    static final String PREVIEW_USER_ID = "local-preview-user";

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping
    public UserProfile getProfile(
        @RequestHeader(name = "X-TrailMate-User-Id", required = false) String userId
    ) {
        return userProfileService.getProfile(resolveUserId(userId));
    }

    @PutMapping
    public UserProfile saveProfile(
        @RequestHeader(name = "X-TrailMate-User-Id", required = false) String userId,
        @Valid @RequestBody UserProfileRequest request
    ) {
        return userProfileService.saveProfile(resolveUserId(userId), request);
    }

    @ExceptionHandler(UserProfileNotFoundException.class)
    public ResponseEntity<UserProfileApiErrorResponse> handleNotFound(UserProfileNotFoundException exception) {
        return ResponseEntity
            .status(404)
            .body(new UserProfileApiErrorResponse(
                404,
                "USER_PROFILE_NOT_FOUND",
                exception.getMessage(),
                UUID.randomUUID().toString()
            ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<UserProfileApiErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity
            .badRequest()
            .body(new UserProfileApiErrorResponse(
                400,
                "USER_PROFILE_INVALID_REQUEST",
                exception.getMessage() == null ? "Invalid user profile request." : exception.getMessage(),
                UUID.randomUUID().toString()
            ));
    }

    private String resolveUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return PREVIEW_USER_ID;
        }
        return userId.trim();
    }
}
