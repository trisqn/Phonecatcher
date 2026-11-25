package com.example.javaproject.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Public category details for creating reports.
 *
 * @param id category identifier
 * @param code stable category code
 * @param name human-readable category name
 */
@Schema(description = "Active category available for use in new reports.")
public record CategoryResponse(
        @Schema(description = "Category identifier.") Long id,
        @Schema(description = "Stable category code.", example = "POLICE") String code,
        @Schema(description = "Human-readable category name.", example = "Police officer") String name
) {
}
