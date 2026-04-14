package com.mcart.user.controller;

import com.mcart.user.dto.UserAddressRequest;
import com.mcart.user.dto.UserAddressResponse;
import com.mcart.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/user/addresses")
@RequiredArgsConstructor
public class UserAddressController {

    private static final String CLAIM_USER_ID = "userId";

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserAddressResponse>> list(@AuthenticationPrincipal Jwt jwt) {
        var gate = gatekeep(jwt);
        if (gate.response != null) {
            return narrow(gate.response);
        }
        return ResponseEntity.ok(userService.listAddresses(gate.userId));
    }

    @GetMapping("/default")
    public ResponseEntity<UserAddressResponse> getDefault(@AuthenticationPrincipal Jwt jwt) {
        var gate = gatekeep(jwt);
        if (gate.response != null) {
            return narrow(gate.response);
        }
        return userService.getDefaultAddress(gate.userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<UserAddressResponse> getById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("addressId") UUID addressId
    ) {
        var gate = gatekeep(jwt);
        if (gate.response != null) {
            return narrow(gate.response);
        }
        return userService.getAddressById(gate.userId, addressId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UserAddressResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody UserAddressRequest req
    ) {
        var gate = gatekeep(jwt);
        if (gate.response != null) {
            return narrow(gate.response);
        }
        UserAddressResponse created = userService.addAddress(gate.userId, req != null ? req : new UserAddressRequest());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/{addressId}/default")
    public ResponseEntity<UserAddressResponse> setDefault(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("addressId") UUID addressId
    ) {
        var gate = gatekeep(jwt);
        if (gate.response != null) {
            return narrow(gate.response);
        }
        return userService.setDefault(gate.userId, addressId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private GateResult gatekeep(Jwt jwt) {
        if (jwt == null) {
            return new GateResult(null, ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        String userIdClaim = jwt.getClaim(CLAIM_USER_ID);
        if (userIdClaim == null || userIdClaim.isBlank()) {
            return new GateResult(null, ResponseEntity.badRequest().build());
        }
        final UUID userId;
        try {
            userId = UUID.fromString(userIdClaim.trim());
        } catch (IllegalArgumentException ex) {
            return new GateResult(null, ResponseEntity.badRequest().build());
        }

        var accessOpt = userService.getProfileAccessByUserId(userId);
        if (accessOpt.isEmpty()) {
            return new GateResult(null, ResponseEntity.notFound().build());
        }
        if (!accessOpt.get().emailVerified()) {
            return new GateResult(null, ResponseEntity.status(HttpStatus.FORBIDDEN).build());
        }
        return new GateResult(userId, null);
    }

    /**
     * Error responses are bodiless status-only; safe to narrow for the declared controller return type.
     */
    @SuppressWarnings("unchecked")
    private static <T> ResponseEntity<T> narrow(ResponseEntity<?> response) {
        return (ResponseEntity<T>) response;
    }

    private record GateResult(UUID userId, ResponseEntity<?> response) {}
}

