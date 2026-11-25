package com.example.javaproject.repository;

import com.example.javaproject.entity.FraudCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Persistence operations for active and historical fraud categories.
 */
public interface FraudCategoryRepository extends JpaRepository<FraudCategory, Long> {

    /**
     * Finds reportable categories alphabetically.
     *
     * @return active categories ordered by name
     */
    List<FraudCategory> findAllByActiveTrueOrderByNameAsc();

    /**
     * Finds all categories, including inactive history.
     *
     * @return all categories ordered by name
     */
    List<FraudCategory> findAllByOrderByNameAsc();

    /**
     * Finds a reportable category by its stable code.
     *
     * @param code stable category code
     * @return matching active category, if present
     */
    Optional<FraudCategory> findByCodeAndActiveTrue(String code);

    /**
     * Finds a category by its stable code regardless of active state.
     *
     * @param code stable category code
     * @return matching category, if present
     */
    Optional<FraudCategory> findByCode(String code);

    /**
     * Checks whether a category code is already allocated.
     *
     * @param code stable category code
     * @return {@code true} when the code is already in use
     */
    boolean existsByCode(String code);
}
