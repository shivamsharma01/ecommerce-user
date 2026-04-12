package com.mcart.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
