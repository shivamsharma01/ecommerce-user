package com.mcart.user.repository;

import com.mcart.user.entity.User;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link User} entity persistence.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Finds a user by their unique identifier (synced from auth service).
     *
     * @param userId the user ID
     * @return the user if found
     */
    Optional<User> findByUserId(UUID userId);

    /**
     * Checks if a user exists with the given email.
     *
     * @param email the email address
     * @return true if a user exists
     */
    boolean existsByEmail(String email);
}
