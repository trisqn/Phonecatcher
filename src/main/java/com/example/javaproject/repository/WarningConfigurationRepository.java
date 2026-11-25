package com.example.javaproject.repository;

import com.example.javaproject.entity.WarningConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence operations for the singleton warning configuration.
 */
public interface WarningConfigurationRepository extends JpaRepository<WarningConfiguration, Integer> {
}
