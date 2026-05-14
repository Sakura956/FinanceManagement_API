package com.finance.modules.admin.service;

import com.finance.modules.admin.dto.CategoryRequest;

public interface AdminCategoryService {
    Object listCategories(Integer type, boolean includeDisabled);

    Object createCategory(CategoryRequest request);

    Object updateCategory(Long id, CategoryRequest request);

    void deleteCategory(Long id);

    void updateCategoryStatus(Long id, Integer status);
}
