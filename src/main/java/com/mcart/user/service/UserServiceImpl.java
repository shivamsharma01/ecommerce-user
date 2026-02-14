package com.mcart.user.service;

import com.mcart.user.dto.UserResponse;
import com.mcart.user.dto.UserSignupEvent;
import com.mcart.user.entity.User;
import com.mcart.user.mapper.UserMapper;
import com.mcart.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of {@link UserService}.
 * <p>
 * Persists users from auth service signup events and serves profile data for the /me endpoint.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private static final String USER_SIGNUP_COMPLETED = "USER_SIGNUP_COMPLETED";

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public void handleSignupEvent(UserSignupEvent event) {
        if (event == null || event.getPayload() == null || event.getUserId() == null) {
            log.warn("Ignoring invalid signup event: {}", event);
            return;
        }
        if (!USER_SIGNUP_COMPLETED.equals(event.getEventType())) {
            log.debug("Ignoring non-signup event type: {}", event.getEventType());
            return;
        }

        UUID userId = event.getUserId();
        User user = userMapper.toEntity(userId, event.getPayload());

        Optional<User> existing = userRepository.findByUserId(userId);
        if (existing.isPresent()) {
            User existingUser = existing.get();
            existingUser.setEmail(user.getEmail());
            existingUser.setFirstName(user.getFirstName());
            existingUser.setLastName(user.getLastName());
            existingUser.setUpdatedAt(Instant.now());
            userRepository.save(existingUser);
            log.info("Updated user profile for userId={}", userId);
        } else {
            user.setCreatedAt(Instant.now());
            user.setUpdatedAt(Instant.now());
            userRepository.save(user);
            log.info("Created user profile for userId={}", userId);
        }
    }

    @Override
    public Optional<UserResponse> getByUserId(UUID userId) {
        return userRepository.findByUserId(userId)
                .map(userMapper::toResponse);
    }
}
