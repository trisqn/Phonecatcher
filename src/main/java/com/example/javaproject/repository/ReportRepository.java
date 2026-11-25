package com.example.javaproject.repository;

import com.example.javaproject.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Standard persistence operations for submitted reports.
 */
public interface ReportRepository extends JpaRepository<Report, Long> {
}
