package com.mcart.user.service;

import com.mcart.user.dto.UserResponse;
import com.mcart.user.dto.UserSignupEvent;
import com.mcart.user.entity.User;

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
}
