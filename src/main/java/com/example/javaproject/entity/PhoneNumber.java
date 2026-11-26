package com.example.javaproject.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.regex.Pattern;

/**
 * A unique phone number stored in normalized international E.164 format.
 */
@Entity
@Table(
        name = "phone_number",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_phone_number_normalized_number",
                columnNames = "normalized_number"
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PhoneNumber {

    private static final Pattern E164_PATTERN = Pattern.compile("^\\+[1-9][0-9]{1,14}$");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "normalized_number", nullable = false, length = 16)
    private String normalizedNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Creates a phone-number entity from a normalized E.164 number.
     *
     * @param normalizedNumber international number, including the leading plus sign
     * @throws IllegalArgumentException if the number is not in E.164 format
     */
    public PhoneNumber(String normalizedNumber) {
        if (normalizedNumber == null || !E164_PATTERN.matcher(normalizedNumber).matches()) {
            throw new IllegalArgumentException("Phone number must be in E.164 format");
        }
        this.normalizedNumber = normalizedNumber;
    }

    @PrePersist
    void setCreatedAt() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

}
