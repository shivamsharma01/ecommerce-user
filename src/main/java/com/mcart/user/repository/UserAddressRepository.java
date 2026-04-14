package com.mcart.user.repository;

import com.mcart.user.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, UUID> {
    List<UserAddress> findByUserIdOrderByIsDefaultDescCreatedAtDesc(UUID userId);

    long countByUserId(UUID userId);

    Optional<UserAddress> findByAddressIdAndUserId(UUID addressId, UUID userId);

    Optional<UserAddress> findFirstByUserIdAndIsDefault(UUID userId, Boolean isDefault);
}

