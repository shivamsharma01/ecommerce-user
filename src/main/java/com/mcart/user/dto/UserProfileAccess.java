package com.mcart.user.dto;

import java.util.Objects;

public record UserProfileAccess(UserResponse profile, boolean emailVerified) {

    public UserProfileAccess {
        Objects.requireNonNull(profile, "profile must not be null");
    }
}

