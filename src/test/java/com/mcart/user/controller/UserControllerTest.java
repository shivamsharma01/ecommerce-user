package com.mcart.user.controller;

import com.mcart.user.dto.UserProfileAccess;
import com.mcart.user.dto.UserResponse;
import com.mcart.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void getProfile_nullUserIdClaim_returnsBadRequest() {
        Jwt jwt = buildJwtWithUserIdClaim(null);

        ResponseEntity<UserResponse> response = userController.getProfile(jwt);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getProfile_blankUserIdClaim_returnsBadRequest() {
        Jwt jwt = buildJwtWithUserIdClaim("   ");

        ResponseEntity<UserResponse> response = userController.getProfile(jwt);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getProfile_invalidUuidClaim_returnsBadRequest() {
        Jwt jwt = buildJwtWithUserIdClaim("not-a-uuid");

        ResponseEntity<UserResponse> response = userController.getProfile(jwt);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getProfile_userMissing_returnsNotFound() {
        UUID userId = UUID.randomUUID();
        Jwt jwt = buildJwtWithUserIdClaim(userId.toString());

        when(userService.getProfileAccessByUserId(userId)).thenReturn(Optional.empty());

        ResponseEntity<UserResponse> response = userController.getProfile(jwt);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getProfile_userUnverified_returnsForbidden() {
        UUID userId = UUID.randomUUID();
        Jwt jwt = buildJwtWithUserIdClaim(userId.toString());

        UserResponse profile = UserResponse.builder()
                .userId(userId)
                .email("a@example.com")
                .firstName("A")
                .lastName("B")
                .build();

        when(userService.getProfileAccessByUserId(userId))
                .thenReturn(Optional.of(new UserProfileAccess(profile, false)));

        ResponseEntity<UserResponse> response = userController.getProfile(jwt);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getProfile_userVerified_returnsOk() {
        UUID userId = UUID.randomUUID();
        Jwt jwt = buildJwtWithUserIdClaim(userId.toString());

        UserResponse profile = UserResponse.builder()
                .userId(userId)
                .email("a@example.com")
                .firstName("A")
                .lastName("B")
                .build();

        when(userService.getProfileAccessByUserId(userId))
                .thenReturn(Optional.of(new UserProfileAccess(profile, true)));

        ResponseEntity<UserResponse> response = userController.getProfile(jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(profile, response.getBody());
    }

    private Jwt buildJwtWithUserIdClaim(String userIdClaim) {
        Instant now = Instant.now();
        Map<String, Object> headers = Map.of("alg", "none");
        Map<String, Object> claims = new java.util.HashMap<>();
        // Jwt requires at least one claim; use a stable 'sub' and optionally add 'userId'
        claims.put("sub", "test-subject");
        if (userIdClaim != null) {
            claims.put("userId", userIdClaim);
        }

        return new Jwt("token", now, now.plusSeconds(60), headers, claims);
    }
}

