package com.finance.modules.bill.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.finance.common.result.Result;
import com.finance.modules.bill.entity.BillCategory;
import com.finance.modules.bill.mapper.BillCategoryMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户端分类接口 - 提供分类列表供用户创建账单时选择
 * 就一个接口，省事就用一个controller层了，严格严格三层框架分层
 */
@RestController
@RequestMapping("/api/v1/user/categories")
public class CategoryController {
    private final BillCategoryMapper billCategoryMapper;

    public CategoryController(BillCategoryMapper billCategoryMapper) {
        this.billCategoryMapper = billCategoryMapper;
    }

    /**
     * 获取可用分类列表（仅返回启用状态的分类）
     * @param type
     * @return
     */
    @GetMapping
    public Result<?> list(@RequestParam(required = false) Integer type) {
        LambdaQueryWrapper<BillCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BillCategory::getStatus, 0);//只查启用的分类
        if (type != null) {
            wrapper.eq(BillCategory::getType, type);
        }
        wrapper.orderByAsc(BillCategory::getSortOrder);

        List<BillCategory> categories = billCategoryMapper.selectList(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();
        for (BillCategory cat : categories) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", cat.getId());
            item.put("name", cat.getName());
            item.put("type", cat.getType());
            item.put("icon", cat.getIcon());
            result.add(item);
        }
        return Result.success(result);
    }
}
