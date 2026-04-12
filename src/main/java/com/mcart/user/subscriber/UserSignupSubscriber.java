package com.mcart.user.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcart.user.dto.UserSignupEvent;
import com.mcart.user.service.UserService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "user.pubsub.enabled", havingValue = "true", matchIfMissing = false)
public class UserSignupSubscriber {

    private static final String DEFAULT_SUBSCRIPTION = "user-signup-events-sub";

    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final PubSubTemplate pubSubTemplate;

    @Value("${user.pubsub.subscription:" + DEFAULT_SUBSCRIPTION + "}")
    private String subscriptionName;

    private com.google.cloud.pubsub.v1.Subscriber subscriber;

    @PostConstruct
    public void subscribe() {
        subscriber = pubSubTemplate.subscribe(subscriptionName, this::handleMessage);
        log.info("Subscribed to Pub/Sub subscription: {}", subscriptionName);
    }

    @PreDestroy
    public void shutdown() {
        if (subscriber != null) {
            subscriber.stopAsync();
            log.info("Stopped Pub/Sub subscription: {}", subscriptionName);
        }
    }

    private void handleMessage(BasicAcknowledgeablePubsubMessage message) {
        try {
            String payload = message.getPubsubMessage().getData().toStringUtf8();
            UserSignupEvent event = objectMapper.readValue(payload, UserSignupEvent.class);
            userService.handleSignupEvent(event);
            message.ack();
        } catch (Exception ex) {
            log.error("Failed to process user signup event", ex);
            message.nack();
        }
    }
}
