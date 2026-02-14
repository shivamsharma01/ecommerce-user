package com.mcart.user.controller;

import com.mcart.user.dto.UserResponse;
import com.mcart.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for user profile operations.
 * <p>
 * Serves the current user's profile via the /me endpoint, using the JWT issued by the auth service.
 * </p>
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
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
     * @return the user profile, or 404 if the user is not yet synced to this service
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal Jwt jwt) {
        String userIdClaim = jwt.getClaim(CLAIM_USER_ID);
        if (userIdClaim == null) {
            return ResponseEntity.badRequest().build();
        }

        UUID userId = UUID.fromString(userIdClaim);
        return userService.getByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
