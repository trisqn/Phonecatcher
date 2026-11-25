package com.example.javaproject.repository;

import com.example.javaproject.entity.PhoneNumber;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;
import java.util.List;
import java.util.Optional;

/**
 * Persistence operations for normalized phone numbers.
 */
public interface PhoneNumberRepository extends JpaRepository<PhoneNumber, Long> {

    /**
     * Finds and locks a phone-number row by its normalized E.164 value.
     *
     * @param normalizedNumber normalized E.164 number
     * @return the locked phone-number row, if present
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select phone from PhoneNumber phone where phone.normalizedNumber = :normalizedNumber")
    Optional<PhoneNumber> findByNormalizedNumberForUpdate(
            @Param("normalizedNumber") String normalizedNumber
    );

    /**
     * Finds and locks a phone-number row by its identifier.
     *
     * @param id phone-number identifier
     * @return the locked phone-number row, if present
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select phone from PhoneNumber phone where phone.id = :id")
    Optional<PhoneNumber> findByIdForUpdate(@Param("id") Long id);

    /**
     * Returns phone-number identifiers in stable order for batch recalculation.
     *
     * @return all phone-number identifiers ordered by id
     */
    @Query("select phone.id from PhoneNumber phone order by phone.id")
    List<Long> findAllIds();
}
