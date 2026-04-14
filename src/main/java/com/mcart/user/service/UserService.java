package com.mcart.user.service;

import com.mcart.user.dto.UserProfileAccess;
import com.mcart.user.dto.UserSignupEvent;
import com.mcart.user.dto.UserAddressRequest;
import com.mcart.user.dto.UserAddressResponse;
import com.mcart.user.entity.User;
import com.mcart.user.entity.UserAddress;
import com.mcart.user.mapper.UserMapper;
import com.mcart.user.repository.UserAddressRepository;
import com.mcart.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private static final String USER_SIGNUP_COMPLETED = "USER_SIGNUP_COMPLETED";
    private static final String EMAIL_VERIFIED = "EMAIL_VERIFIED";

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserAddressRepository userAddressRepository;

    @Transactional
    public void handleSignupEvent(UserSignupEvent event) {
        if (event == null || event.getUserId() == null) {
            log.warn("Ignoring invalid signup event (missing userId)");
            return;
        }

        if (EMAIL_VERIFIED.equals(event.getEventType())) {
            handleEmailVerifiedEvent(event.getUserId());
            return;
        }

        if (!USER_SIGNUP_COMPLETED.equals(event.getEventType())) {
            log.debug("Ignoring non-signup event type: {}", event.getEventType());
            return;
        }

        if (event.getPayload() == null) {
            log.warn("Ignoring USER_SIGNUP_COMPLETED event with null payload");
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
            // Never downgrade verification on replayed / stale USER_SIGNUP_COMPLETED events
            existingUser.setEmailVerified(
                    existingUser.getEmailVerified() || Boolean.TRUE.equals(user.getEmailVerified()));
            existingUser.setUpdatedAt(Instant.now());
            userRepository.save(existingUser);
            log.info("Updated user profile for userId={}", userId);
        } else {
            user.setCreatedAt(Instant.now());
            user.setUpdatedAt(Instant.now());
            userRepository.save(user);
            log.info("Created user profile for userId={} (verified={})", userId, user.getEmailVerified());
        }
    }

    private void handleEmailVerifiedEvent(UUID userId) {
        userRepository.findByUserId(userId).ifPresentOrElse(
                user -> {
                    user.setEmailVerified(true);
                    user.setUpdatedAt(Instant.now());
                    userRepository.save(user);
                    log.info("Set email_verified=true for userId={}", userId);
                },
                () -> log.warn("EMAIL_VERIFIED event for unknown userId={}", userId)
        );
    }

    public Optional<UserProfileAccess> getProfileAccessByUserId(UUID userId) {
        return userRepository.findByUserId(userId)
                .map(user -> new UserProfileAccess(
                        userMapper.toResponse(user),
                        Boolean.TRUE.equals(user.getEmailVerified())
                ));
    }

    @Transactional(readOnly = true)
    public List<UserAddressResponse> listAddresses(UUID userId) {
        return userAddressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId).stream()
                .map(this::toAddressResponse)
                .toList();
    }

    @Transactional
    public UserAddressResponse addAddress(UUID userId, UserAddressRequest req) {
        UserAddress a = new UserAddress();
        a.setUserId(userId);
        a.setFullName(nullToEmpty(req.getFullName()).trim());
        a.setPhone(nullToEmpty(req.getPhone()).trim());
        a.setLine1(nullToEmpty(req.getLine1()).trim());
        a.setLine2(req.getLine2() != null ? req.getLine2().trim() : null);
        a.setCity(nullToEmpty(req.getCity()).trim());
        a.setState(nullToEmpty(req.getState()).trim());
        a.setPincode(nullToEmpty(req.getPincode()).trim());
        a.setCountry(nullToEmpty(req.getCountry()).trim());

        boolean first = userAddressRepository.countByUserId(userId) == 0;
        boolean makeDefault = Boolean.TRUE.equals(req.getMakeDefault()) || first;
        a.setIsDefault(makeDefault);

        if (makeDefault) {
            unsetDefaultForUser(userId);
        }

        UserAddress saved = userAddressRepository.save(a);
        return toAddressResponse(saved);
    }

    @Transactional
    public Optional<UserAddressResponse> setDefault(UUID userId, UUID addressId) {
        Optional<UserAddress> found = userAddressRepository.findByAddressIdAndUserId(addressId, userId);
        if (found.isEmpty()) return Optional.empty();

        unsetDefaultForUser(userId);
        UserAddress a = found.get();
        a.setIsDefault(true);
        UserAddress saved = userAddressRepository.save(a);
        return Optional.of(toAddressResponse(saved));
    }

    @Transactional(readOnly = true)
    public Optional<UserAddressResponse> getDefaultAddress(UUID userId) {
        return userAddressRepository.findFirstByUserIdAndIsDefault(userId, true)
                .map(this::toAddressResponse);
    }

    @Transactional(readOnly = true)
    public Optional<UserAddressResponse> getAddressById(UUID userId, UUID addressId) {
        return userAddressRepository.findByAddressIdAndUserId(addressId, userId)
                .map(this::toAddressResponse);
    }

    private void unsetDefaultForUser(UUID userId) {
        List<UserAddress> existing = userAddressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
        for (UserAddress e : existing) {
            if (Boolean.TRUE.equals(e.getIsDefault())) {
                e.setIsDefault(false);
                userAddressRepository.save(e);
            }
        }
    }

    private UserAddressResponse toAddressResponse(UserAddress a) {
        return UserAddressResponse.builder()
                .addressId(a.getAddressId())
                .fullName(a.getFullName())
                .phone(a.getPhone())
                .line1(a.getLine1())
                .line2(a.getLine2())
                .city(a.getCity())
                .state(a.getState())
                .pincode(a.getPincode())
                .country(a.getCountry())
                .isDefault(Boolean.TRUE.equals(a.getIsDefault()))
                .build();
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
