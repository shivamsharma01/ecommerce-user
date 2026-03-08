package com.mcart.user.mapper;

import com.mcart.user.dto.UserResponse;
import com.mcart.user.dto.UserSignupEventPayload;
import com.mcart.user.entity.User;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.UUID;

/**
 * MapStruct mapper for {@link User} entity and DTOs.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Maps a user entity to a response DTO.
     *
     * @param user the user entity
     * @return the response DTO
     */
    UserResponse toResponse(User user);

    /**
     * Maps a signup event payload and userId to a new user entity.
     * Handles social signup payloads where firstName/lastName may be null.
     *
     * @param userId  the user ID from the auth service
     * @param payload the signup event payload (email, firstName, lastName)
     * @return a new user entity ready for persistence
     */
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "email", source = "payload.email")
    @Mapping(target = "firstName", source = "payload.firstName")
    @Mapping(target = "lastName", source = "payload.lastName")
    @Mapping(target = "emailVerified", source = "payload.verified")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(UUID userId, UserSignupEventPayload payload);

    @AfterMapping
    default void ensureNonNullNames(@MappingTarget User user) {
        if (user.getFirstName() == null) user.setFirstName("");
        if (user.getLastName() == null) user.setLastName("");
        if (user.getEmailVerified() == null) user.setEmailVerified(false);
    }
}
