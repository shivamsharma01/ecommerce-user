package com.mcart.user.controller;

import com.mcart.user.dto.UserResponse;
import com.mcart.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for user profile operations.
 * <p>
 * Serves the current user's profile via the /me and /profile endpoints, using the JWT issued by the auth service.
 * Profile data is synced from the auth service via Pub/Sub when users sign up.
 * </p>
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "Endpoints for fetching user profile details")
public class UserController {

    private static final String CLAIM_USER_ID = "userId";

    private final UserService userService;

    /**
     * Returns the profile of the currently authenticated user.
     * <p>
     * The user ID is extracted from the JWT {@code userId} claim. User profile data
     * is synced from the auth service via Pub/Sub when users sign up.
     * </p>
     *
     * @param jwt the JWT from the auth service (subject = authIdentityId, claim userId = userId)
     * @return the user profile; 404 if user is missing, 403 if user is present but not email-verified
     */
    @GetMapping(value = {"/me", "/profile"})
    @Operation(
            summary = "Get current user profile",
            description = "Returns the profile details of the authenticated user. Requires a valid JWT from the auth service. Returns 403 if the user exists but is not email-verified."
    )
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<UserResponse> getProfile(@AuthenticationPrincipal Jwt jwt) {
        String userIdClaim = jwt.getClaim(CLAIM_USER_ID);
        if (userIdClaim == null || userIdClaim.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        final UUID userId;
        try {
            userId = UUID.fromString(userIdClaim.trim());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }

        var accessOpt = userService.getProfileAccessByUserId(userId);
        if (accessOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var access = accessOpt.get();
        if (access.emailVerified()) {
            return ResponseEntity.ok(access.profile());
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
}
