package com.mcart.user.dto;

import java.util.Objects;

/**
 * Profile access details for the currently authenticated user.
 * <p>
 * Used to distinguish between:
 * <ul>
 *     <li>User does not exist in this service ({@code 404}).</li>
 *     <li>User exists but is not email-verified ({@code 403}).</li>
 * </ul>
 */
public record UserProfileAccess(UserResponse profile, boolean emailVerified) {

    public UserProfileAccess {
        Objects.requireNonNull(profile, "profile must not be null");
    }
}

