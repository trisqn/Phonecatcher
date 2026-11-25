package com.example.javaproject.category.service;

import com.example.javaproject.category.dto.CategoryResponse;
import com.example.javaproject.category.dto.AdminCategoryResponse;
import com.example.javaproject.category.dto.CreateCategoryRequest;
import com.example.javaproject.category.dto.UpdateCategoryRequest;
import com.example.javaproject.entity.FraudCategory;
import com.example.javaproject.repository.FraudCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * Implements category lookup and administrator management operations.
 */
@Service
public class CategoryService {

    private final FraudCategoryRepository categoryRepository;

    /**
     * Creates the service with its category repository.
     *
     * @param categoryRepository persistent category storage
     */
    public CategoryService(FraudCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Retrieves active categories for public selection.
     *
     * @return active categories ordered by name
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getActiveCategories() {
        return categoryRepository.findAllByActiveTrueOrderByNameAsc().stream()
                .map(category -> new CategoryResponse(category.getId(), category.getCode(), category.getName()))
                .toList();
    }

    /**
     * Retrieves all categories for administrator management.
     *
     * @return active and inactive categories ordered by name
     */
    @Transactional(readOnly = true)
    public List<AdminCategoryResponse> getAllCategories() {
        return categoryRepository.findAllByOrderByNameAsc().stream()
                .map(this::toAdminResponse)
                .toList();
    }

    /**
     * Creates a category, normalizing its code to uppercase.
     *
     * @param request validated category fields
     * @return the persisted category
     * @throws CategoryCodeAlreadyExistsException if the normalized code is already assigned
     */
    @Transactional
    public AdminCategoryResponse createCategory(CreateCategoryRequest request) {
        String code = request.code().trim().toUpperCase(Locale.ROOT);
        if (categoryRepository.existsByCode(code)) {
            throw new CategoryCodeAlreadyExistsException(code);
        }

        FraudCategory category = categoryRepository.save(new FraudCategory(
                code,
                request.name().trim(),
                request.warningTitle().trim()
        ));
        return toAdminResponse(category);
    }

    /**
     * Updates a category's editable fields.
     *
     * @param id category identifier
     * @param request new display fields and active state
     * @return the updated category
     * @throws CategoryNotFoundException if the category does not exist
     */
    @Transactional
    public AdminCategoryResponse updateCategory(Long id, UpdateCategoryRequest request) {
        FraudCategory category = getCategory(id);
        category.update(request.name().trim(), request.warningTitle().trim(), request.active());
        return toAdminResponse(category);
    }

    /**
     * Marks a category inactive while preserving its historical references.
     *
     * @param id category identifier
     * @return the deactivated category
     * @throws CategoryNotFoundException if the category does not exist
     */
    @Transactional
    public AdminCategoryResponse deactivateCategory(Long id) {
        FraudCategory category = getCategory(id);
        category.deactivate();
        return toAdminResponse(category);
    }

    private FraudCategory getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    private AdminCategoryResponse toAdminResponse(FraudCategory category) {
        return new AdminCategoryResponse(
                category.getId(),
                category.getCode(),
                category.getName(),
                category.getWarningTitle(),
                category.isActive()
        );
    }
}
