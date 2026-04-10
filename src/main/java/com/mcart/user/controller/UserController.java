package com.mcart.user.controller;

import com.mcart.user.dto.UserResponse;
import com.mcart.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private static final String CLAIM_USER_ID = "userId";

    private final UserService userService;

    @GetMapping(value = {"/me", "/profile"})
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
