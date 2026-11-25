package com.example.javaproject.category.controller;

import com.example.javaproject.auth.controller.AuthExceptionHandler;
import com.example.javaproject.category.dto.AdminCategoryResponse;
import com.example.javaproject.category.dto.CreateCategoryRequest;
import com.example.javaproject.category.dto.UpdateCategoryRequest;
import com.example.javaproject.category.service.CategoryNotFoundException;
import com.example.javaproject.category.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Provides category-management endpoints restricted to administrators.
 */
@RestController
@RequestMapping("/api/v1/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Category administration", description = "Create and maintain categories. Requires the ADMIN role.")
@SecurityRequirement(name = "bearerAuth")
public class AdminCategoryController {

    private final CategoryService categoryService;

    /**
     * Creates the controller with its category service.
     *
     * @param categoryService category management operations
     */
    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Lists every category, including categories retained as inactive.
     *
     * @return all categories ordered by display name
     */
    @GetMapping
    @Operation(summary = "List all categories", description = "Returns active and inactive categories for administrators.")
    public List<AdminCategoryResponse> getCategories() {
        return categoryService.getAllCategories();
    }

    /**
     * Creates an active category with a normalized, unique code.
     *
     * @param request category code, display name, and warning title
     * @return the created category
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a category",
            description = "Creates an active category. The code is trimmed and normalized to uppercase and must be unique.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Category created"),
                    @ApiResponse(responseCode = "400", description = "Request validation failed",
                            content = @Content(schema = @Schema(implementation = AuthExceptionHandler.ApiError.class))),
                    @ApiResponse(responseCode = "409", description = "Category code already exists",
                            content = @Content(schema = @Schema(implementation = AuthExceptionHandler.ApiError.class)))
            })
    public AdminCategoryResponse createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        return categoryService.createCategory(request);
    }

    /**
     * Updates category display fields and active state.
     *
     * @param id category identifier
     * @param request updated name, warning title, and active state
     * @return the updated category
     * @throws CategoryNotFoundException if the category does not exist
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Update or reactivate a category",
            description = "Updates its display name, warning title, and active state. Use active=false to deactivate and active=true to reactivate. Reports already linked to it are retained.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Category updated"),
                    @ApiResponse(responseCode = "400", description = "Request validation failed",
                            content = @Content(schema = @Schema(implementation = AuthExceptionHandler.ApiError.class))),
                    @ApiResponse(responseCode = "404", description = "Category does not exist",
                            content = @Content(schema = @Schema(implementation = AuthExceptionHandler.ApiError.class)))
            })
    public AdminCategoryResponse updateCategory(
            @Parameter(description = "Category identifier", example = "1") @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {
        return categoryService.updateCategory(id, request);
    }

    /**
     * Deactivates a category without deleting its report history.
     *
     * @param id category identifier
     * @return the deactivated category
     * @throws CategoryNotFoundException if the category does not exist
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Deactivate a category",
            description = "Soft-deletes the category by marking it inactive. Existing reports and warnings are not deleted.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Category deactivated"),
                    @ApiResponse(responseCode = "404", description = "Category does not exist",
                            content = @Content(schema = @Schema(implementation = AuthExceptionHandler.ApiError.class)))
            })
    public AdminCategoryResponse deactivateCategory(
            @Parameter(description = "Category identifier", example = "1") @PathVariable Long id
    ) {
        return categoryService.deactivateCategory(id);
    }
}
