package com.example.javaproject.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Input for creating a category.
 *
 * @param code unique stable code
 * @param name human-readable category name
 * @param warningTitle title displayed on warnings for this category
 */
@Schema(description = "New impersonation category. Newly created categories are active.")
public record CreateCategoryRequest(
        @Schema(description = "Unique stable code; letters, digits and underscores only. It is normalized to uppercase.", example = "UTILITY_WORKER")
        @NotBlank
        @Size(max = 50)
        @Pattern(regexp = "^[A-Za-z][A-Za-z0-9_]*$")
        String code,
        @Schema(description = "Human-readable category name.", example = "Utility worker")
        @NotBlank @Size(max = 100) String name,
        @Schema(description = "Title shown on warnings for this category.", example = "Scammer posing as a utility worker")
        @NotBlank @Size(max = 200) String warningTitle
) {
}
