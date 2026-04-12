package com.mcart.user.mapper;

import com.mcart.user.dto.UserResponse;
import com.mcart.user.dto.UserSignupEventPayload;
import com.mcart.user.entity.User;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);

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
