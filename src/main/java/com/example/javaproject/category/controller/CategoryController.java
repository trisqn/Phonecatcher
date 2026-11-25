package com.example.javaproject.category.controller;

import com.example.javaproject.category.dto.CategoryResponse;
import com.example.javaproject.category.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Provides public lookup of categories available for reporting.
 */
@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Categories", description = "Public lookup of categories available for new reports.")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Creates the controller with its category service.
     *
     * @param categoryService category lookup operations
     */
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Lists active categories available for new reports.
     *
     * @return active categories ordered by display name
     */
    @GetMapping
    @Operation(
            summary = "List active categories",
            description = "Public endpoint. Only active categories are returned; inactive categories cannot be used for new reports."
    )
    public List<CategoryResponse> getActiveCategories() {
        return categoryService.getActiveCategories();
    }
}
