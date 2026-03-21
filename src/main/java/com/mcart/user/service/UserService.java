package com.mcart.user.service;

import com.mcart.user.dto.UserResponse;
import com.mcart.user.dto.UserProfileAccess;
import com.mcart.user.dto.UserSignupEvent;

import java.util.Optional;
import java.util.UUID;

/**
 * Service for user profile operations.
 * <p>
 * Handles creation of users from auth service events and retrieval of user profiles.
 * </p>
 */
public interface UserService {

    /**
     * Creates or updates a user from a signup event published by the auth service.
     *
     * @param event the deserialized signup event
     */
    void handleSignupEvent(UserSignupEvent event);

    /**
     * Retrieves the user profile by user ID.
     *
     * @param userId the user ID (from JWT claim)
     * @return the user profile if found
     */
    Optional<UserResponse> getByUserId(UUID userId);

    /**
     * Retrieves the user profile and email verification status.
     * <p>
     * Allows the controller to distinguish between {@code 404} (user missing)
     * and {@code 403} (user present but unverified).
     * </p>
     */
    Optional<UserProfileAccess> getProfileAccessByUserId(UUID userId);
}
