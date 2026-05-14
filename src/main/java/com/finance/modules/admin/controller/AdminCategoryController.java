package com.finance.modules.admin.controller;

import com.finance.common.result.Result;
import com.finance.modules.admin.dto.CategoryRequest;
import com.finance.modules.admin.service.AdminCategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/categories")
public class AdminCategoryController {

    private final AdminCategoryService adminCategoryService;

    public AdminCategoryController(AdminCategoryService adminCategoryService) {
        this.adminCategoryService = adminCategoryService;
    }


    /**
     * 账单分类列表
     * @param type
     * @param includeDisabled
     * @return
     */
    @GetMapping
    public Result<?> list(@RequestParam(required = false) Integer type,
                          @RequestParam(defaultValue = "false") boolean includeDisabled) {
        return Result.success(adminCategoryService.listCategories(type, includeDisabled));
    }

    /**
     * 新增账单分类
     * @param request
     * @return
     */
    @PostMapping
    public Result<?> create(@RequestBody CategoryRequest request) {
        return Result.success("分类添加成功", adminCategoryService.createCategory(request));
    }


    /**
     * 修改账单分类
     * @param id
     * @param request
     * @return
     */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody CategoryRequest request) {
        return Result.success("分类更新成功", adminCategoryService.updateCategory(id, request));
    }


    /**
     * 删除/禁用账单分类
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminCategoryService.deleteCategory(id);
        return Result.success("分类删除成功", null);
    }


    /**
     * 启用/禁用分类
     * @param id
     * @param body
     * @return
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        adminCategoryService.updateCategoryStatus(id, body.get("status"));
        return Result.success("分类状态更新成功", null);
    }
}
