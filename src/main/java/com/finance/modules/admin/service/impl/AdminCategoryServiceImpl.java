package com.finance.modules.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.finance.common.exception.BusinessException;
import com.finance.modules.admin.dto.CategoryRequest;
import com.finance.modules.admin.service.AdminCategoryService;
import com.finance.modules.bill.entity.BillCategory;
import com.finance.modules.bill.entity.BillRecord;
import com.finance.modules.bill.mapper.BillCategoryMapper;
import com.finance.modules.bill.mapper.BillRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminCategoryServiceImpl implements AdminCategoryService {

    private final BillCategoryMapper billCategoryMapper;
    private final BillRecordMapper billRecordMapper;

    public AdminCategoryServiceImpl(BillCategoryMapper billCategoryMapper,
                                     BillRecordMapper billRecordMapper) {
        this.billCategoryMapper = billCategoryMapper;
        this.billRecordMapper = billRecordMapper;
    }

    /**
     * 管理员查询分类列表（带统计）
     * @param type
     * @param includeDisabled
     * @return
     */
    @Override
    public List<?> listCategories(Integer type, boolean includeDisabled) {
        LambdaQueryWrapper<BillCategory> wrapper = new LambdaQueryWrapper<>();
        // 如果传入了 type（0支出/1收入），就按类型筛选
        if (type != null) {
            wrapper.eq(BillCategory::getType, type);
        }
        // 如果不包含禁用的，就只查 status=0（启用）
        if (!includeDisabled) {
            wrapper.eq(BillCategory::getStatus, 0);
        }
        // 按排序号升序
        wrapper.orderByAsc(BillCategory::getSortOrder);

        //查询分类列表
        List<BillCategory> categories = billCategoryMapper.selectList(wrapper);

        //封装返回 + 统计每个分类下有多少账单
        List<Map<String, Object>> result = new ArrayList<>();
        for (BillCategory cat : categories) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", cat.getId());
            item.put("name", cat.getName());
            item.put("type", cat.getType());
            item.put("icon", cat.getIcon());
            item.put("sortOrder", cat.getSortOrder());
            item.put("isDefault", cat.getIsDefault());
            item.put("status", cat.getStatus());
            // 统计该分类下的账单数量
            Long billCount = billRecordMapper.selectCount(
                    new LambdaQueryWrapper<BillRecord>().eq(BillRecord::getCategoryId, cat.getId()));
            item.put("billCount", billCount);
            result.add(item);
        }
        return result;
    }

    @Override
    public Object createCategory(CategoryRequest request) {
        // 检查同名分类
        Long count = billCategoryMapper.selectCount(
                new LambdaQueryWrapper<BillCategory>()
                        .eq(BillCategory::getType, request.getType())
                        .eq(BillCategory::getName, request.getName()));
        if (count > 0) {
            throw new BusinessException(409, "该类型下已存在同名分类");
        }

        BillCategory category = new BillCategory();
        category.setName(request.getName());
        category.setType(request.getType());
        category.setIcon(request.getIcon());
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        category.setIsDefault(0);
        category.setStatus(0);
        billCategoryMapper.insert(category);
        return category;
    }

    @Override
    public Object updateCategory(Long id, CategoryRequest request) {
        BillCategory category = billCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(404, "分类不存在");
        }
        if (StringUtils.hasText(request.getName())) {
            category.setName(request.getName());
        }
        if (request.getType() != null) {
            category.setType(request.getType());
        }
        if (request.getIcon() != null) {
            category.setIcon(request.getIcon());
        }
        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }
        billCategoryMapper.updateById(category);
        return category;
    }

    @Override
    public void deleteCategory(Long id) {
        BillCategory category = billCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(404, "分类不存在");
        }
        if (category.getIsDefault() != null && category.getIsDefault() == 1) {
            throw new BusinessException(400, "系统默认分类不可删除");
        }
        Long billCount = billRecordMapper.selectCount(
                new LambdaQueryWrapper<BillRecord>().eq(BillRecord::getCategoryId, id));
        if (billCount > 0) {
            Map<String, Object> data = new HashMap<>();
            data.put("billCount", billCount);
            throw new BusinessException(409,
                    "该分类下存在 " + billCount + " 条账单记录，无法删除。建议改为禁用");
        }
        billCategoryMapper.deleteById(id);
    }

    @Override
    public void updateCategoryStatus(Long id, Integer status) {
        BillCategory category = billCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(404, "分类不存在");
        }
        category.setStatus(status);
        billCategoryMapper.updateById(category);
    }
}
