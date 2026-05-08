package com.swaply.userservice.repository;

import com.swaply.userservice.entity.User;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
@Repository

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findById(UUID uuid);

    Optional<User> findByPhone(String phone);

    Optional<User> findByEmail(String email);

    User getUserById(UUID id);
}
