package com.mcart.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Payload structure for {@code USER_SIGNUP_COMPLETED} events published by the auth service
 * to the {@code user-signup-events} Pub/Sub topic.
 * <p>
 * Matches the payload written by {@code AuthServiceImpl.signupWithPassword}.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSignupEventPayload {

    private String email;
    private String firstName;
    private String lastName;
    /** false for password signup; true for social or after EMAIL_VERIFIED event */
    private Boolean verified;
}
