package com.mcart.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSignupEvent {

    @JsonProperty("eventType")
    private String eventType;

    @JsonProperty("aggregateType")
    private String aggregateType;

    @JsonProperty("userId")
    private UUID userId;

    @JsonProperty("authIdentityId")
    private UUID authIdentityId;

    @JsonProperty("payload")
    private UserSignupEventPayload payload;

    @JsonProperty("occurredAt")
    private Instant occurredAt;

    @JsonProperty("version")
    private Integer version;
}
