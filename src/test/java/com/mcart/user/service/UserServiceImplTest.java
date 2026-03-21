package com.mcart.user.service;

import com.mcart.user.dto.UserSignupEvent;
import com.mcart.user.dto.UserSignupEventPayload;
import com.mcart.user.entity.User;
import com.mcart.user.mapper.UserMapper;
import com.mcart.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void handleSignupEvent_nullEvent_isIgnored() {
        verifyNoInteractions(userRepository, userMapper);
        userService.handleSignupEvent(null);
        verifyNoInteractions(userRepository, userMapper);
    }

    @Test
    void handleSignupEvent_nullPayloadForSignup_isIgnored() {
        UserSignupEvent event = UserSignupEvent.builder()
                .eventType("USER_SIGNUP_COMPLETED")
                .userId(UUID.randomUUID())
                .payload(null)
                .build();

        userService.handleSignupEvent(event);
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(userMapper);
    }

    @Test
    void handleSignupEvent_emailVerified_setsTrueWhenUserExists() {
        UUID userId = UUID.randomUUID();
        User existing = User.builder()
                .userId(userId)
                .email("a@example.com")
                .firstName("A")
                .lastName("B")
                .emailVerified(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(userRepository.findByUserId(eq(userId))).thenReturn(Optional.of(existing));

        UserSignupEvent event = UserSignupEvent.builder()
                .eventType("EMAIL_VERIFIED")
                .userId(userId)
                .build();

        userService.handleSignupEvent(event);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(captor.capture());
        assertTrue(captor.getValue().getEmailVerified(), "emailVerified should be set to true");
        assertNotNull(captor.getValue().getUpdatedAt(), "updatedAt should be set");
    }

    @Test
    void handleSignupEvent_userSignupCompleted_doesNotDowngradeVerified() {
        UUID userId = UUID.randomUUID();
        User existing = User.builder()
                .userId(userId)
                .email("a@example.com")
                .firstName("OldFirst")
                .lastName("OldLast")
                .emailVerified(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        User mappedUser = User.builder()
                .userId(userId)
                .email("a@example.com")
                .firstName("NewFirst")
                .lastName("NewLast")
                .emailVerified(false) // stale / replayed payload
                .createdAt(null)
                .updatedAt(null)
                .build();

        UserSignupEventPayload payload = UserSignupEventPayload.builder()
                .email("a@example.com")
                .firstName("NewFirst")
                .lastName("NewLast")
                .verified(false)
                .build();

        when(userRepository.findByUserId(eq(userId))).thenReturn(Optional.of(existing));
        when(userMapper.toEntity(eq(userId), eq(payload))).thenReturn(mappedUser);

        UserSignupEvent event = UserSignupEvent.builder()
                .eventType("USER_SIGNUP_COMPLETED")
                .userId(userId)
                .payload(payload)
                .build();

        userService.handleSignupEvent(event);

        assertTrue(existing.getEmailVerified(), "emailVerified must not be downgraded");
        assertEquals("NewFirst", existing.getFirstName());
        assertEquals("NewLast", existing.getLastName());
        verify(userRepository, times(1)).save(existing);
    }

    @Test
    void handleSignupEvent_userSignupCompleted_createsUserWhenMissing() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findByUserId(eq(userId))).thenReturn(Optional.empty());

        User mappedUser = User.builder()
                .userId(userId)
                .email("a@example.com")
                .firstName("First")
                .lastName("Last")
                .emailVerified(false)
                .createdAt(null)
                .updatedAt(null)
                .build();

        UserSignupEventPayload payload = UserSignupEventPayload.builder()
                .email("a@example.com")
                .firstName("First")
                .lastName("Last")
                .verified(false)
                .build();

        when(userMapper.toEntity(eq(userId), eq(payload))).thenReturn(mappedUser);

        UserSignupEvent event = UserSignupEvent.builder()
                .eventType("USER_SIGNUP_COMPLETED")
                .userId(userId)
                .payload(payload)
                .build();

        userService.handleSignupEvent(event);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(captor.capture());
        assertNotNull(captor.getValue().getCreatedAt(), "createdAt should be set on create");
        assertNotNull(captor.getValue().getUpdatedAt(), "updatedAt should be set on create");
        assertEquals(false, captor.getValue().getEmailVerified());
    }
}

