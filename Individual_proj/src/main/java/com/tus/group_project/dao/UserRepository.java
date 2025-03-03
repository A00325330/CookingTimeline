package com.tus.group_project.dao;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import com.tus.group_project.model.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by email.
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user exists with the given email.
     */
    boolean existsByEmail(String email);

    /**
     * Delete a user by email.
     */
    @Transactional
    @Modifying
    void deleteByEmail(String email);
}
