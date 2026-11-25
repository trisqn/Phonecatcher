package com.example.javaproject.repository;

import com.example.javaproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Persistence operations for application accounts.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds an account without regard to email letter case.
     *
     * @param email email address to search for
     * @return matching account, if present
     */
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Checks whether an account email is already registered, ignoring case.
     *
     * @param email email address to check
     * @return {@code true} if an account uses the email address
     */
    boolean existsByEmailIgnoreCase(String email);
}
