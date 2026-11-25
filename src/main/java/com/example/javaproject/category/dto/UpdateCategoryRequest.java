package com.example.javaproject.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Editable category fields used to update or reactivate a category.
 *
 * @param name human-readable category name
 * @param warningTitle title displayed on warnings for this category
 * @param active whether new reports may use the category
 */
@Schema(description = "Editable category fields. Setting active=false deactivates the category without deleting history.")
public record UpdateCategoryRequest(
        @Schema(description = "Human-readable category name.", example = "Utility worker")
        @NotBlank @Size(max = 100) String name,
        @Schema(description = "Title shown on warnings for this category.", example = "Scammer posing as a utility worker")
        @NotBlank @Size(max = 200) String warningTitle,
        @Schema(description = "Whether the category can be selected in new reports.", example = "true")
        @NotNull Boolean active
) {
}
