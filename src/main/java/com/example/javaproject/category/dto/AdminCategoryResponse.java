package com.example.javaproject.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Category details returned by administrator endpoints.
 *
 * @param id category identifier
 * @param code unique stable code
 * @param name human-readable category name
 * @param warningTitle warning title used for this category
 * @param active whether the category is available for new reports
 */
@Schema(description = "Category details visible to administrators, including inactive categories.")
public record AdminCategoryResponse(
        @Schema(description = "Category identifier.") Long id,
        @Schema(description = "Unique stable code.") String code,
        @Schema(description = "Human-readable category name.") String name,
        @Schema(description = "Warning title for this impersonation category.") String warningTitle,
        @Schema(description = "Inactive categories remain in history but are unavailable for new reports.") boolean active
) {
}
