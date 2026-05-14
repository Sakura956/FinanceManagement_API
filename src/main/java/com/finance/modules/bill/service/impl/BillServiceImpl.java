package com.finance.modules.bill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.finance.common.exception.BusinessException;
import com.finance.common.result.PageResult;
import com.finance.modules.bill.dto.BillCreateRequest;
import com.finance.modules.bill.dto.BillUpdateRequest;
import com.finance.modules.bill.dto.BillVO;
import com.finance.modules.bill.entity.BillCategory;
import com.finance.modules.bill.entity.BillRecord;
import com.finance.modules.bill.mapper.BillCategoryMapper;
import com.finance.modules.bill.mapper.BillRecordMapper;
import com.finance.modules.bill.service.BillService;
import com.finance.util.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BillServiceImpl implements BillService {

    private final BillRecordMapper billRecordMapper;
    private final BillCategoryMapper billCategoryMapper;

    public BillServiceImpl(BillRecordMapper billRecordMapper, BillCategoryMapper billCategoryMapper) {
        this.billRecordMapper = billRecordMapper;
        this.billCategoryMapper = billCategoryMapper;
    }

    @Override
    public Object createBill(BillCreateRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        // 校验分类
        BillCategory category = billCategoryMapper.selectById(request.getCategoryId());
        if (category == null || category.getStatus() == 1) {
            throw new BusinessException(400, "分类不存在或已禁用");
        }
        if (!category.getType().equals(request.getType())) {
            throw new BusinessException(400, "选择的分类与账单类型不匹配");
        }
        // 校验recordTime不能是未来时间
        LocalDateTime recordTime = LocalDateTime.parse(request.getRecordTime(),
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        if (recordTime.isAfter(LocalDateTime.now())) {
            throw new BusinessException(400, "记录时间不能是未来时间");
        }

        BillRecord record = new BillRecord();
        record.setUserId(userId);
        record.setType(request.getType());
        record.setAmount(request.getAmount());
        record.setCategoryId(request.getCategoryId());
        record.setDescription(request.getDescription());
        record.setRecordTime(recordTime);
        billRecordMapper.insert(record);

        Map<String, Object> result = new HashMap<>();
        result.put("id", record.getId());
        result.put("type", record.getType());
        result.put("amount", record.getAmount());
        result.put("categoryId", record.getCategoryId());
        result.put("categoryName", category.getName());
        result.put("categoryIcon", category.getIcon());
        result.put("description", record.getDescription());
        result.put("recordTime", request.getRecordTime());
        result.put("createTime", record.getCreateTime() != null ? record.getCreateTime().toString() : null);
        return result;
    }

    @Override
    public Object listBills(int page, int size, Integer type, Long categoryId,
                                   String startDate, String endDate, String keyword,
                                   Double minAmount, Double maxAmount, String sortBy, String order) {
        Long userId = SecurityUtil.getCurrentUserId();
        LambdaQueryWrapper<BillRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BillRecord::getUserId, userId);
        if (type != null) {
            wrapper.eq(BillRecord::getType, type);
        }
        if (categoryId != null) {
            wrapper.eq(BillRecord::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(startDate)) {
            wrapper.ge(BillRecord::getRecordTime, startDate + " 00:00:00");
        }
        if (StringUtils.hasText(endDate)) {
            wrapper.le(BillRecord::getRecordTime, endDate + " 23:59:59");
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(BillRecord::getDescription, keyword);
        }
        if (minAmount != null) {
            wrapper.ge(BillRecord::getAmount, minAmount);
        }
        if (maxAmount != null) {
            wrapper.le(BillRecord::getAmount, maxAmount);
        }
        // 排序
        if ("amount".equals(sortBy)) {
            if ("asc".equalsIgnoreCase(order)) {
                wrapper.orderByAsc(BillRecord::getAmount);
            } else {
                wrapper.orderByDesc(BillRecord::getAmount);
            }
        } else {
            if ("asc".equalsIgnoreCase(order)) {
                wrapper.orderByAsc(BillRecord::getRecordTime);
            } else {
                wrapper.orderByDesc(BillRecord::getRecordTime);
            }
        }

        Page<BillRecord> result = billRecordMapper.selectPage(new Page<>(page, size), wrapper);

        // 构建VO列表
        List<BillVO> records = new ArrayList<>();
        for (BillRecord record : result.getRecords()) {
            BillCategory category = billCategoryMapper.selectById(record.getCategoryId());
            BillVO vo = new BillVO();
            vo.setId(record.getId());
            vo.setType(record.getType());
            vo.setAmount(record.getAmount());
            vo.setCategoryId(record.getCategoryId());
            vo.setCategoryName(category != null ? category.getName() : null);
            vo.setCategoryIcon(category != null ? category.getIcon() : null);
            vo.setDescription(record.getDescription());
            vo.setRecordTime(record.getRecordTime() != null ? record.getRecordTime().toString() : null);
            vo.setCreateTime(record.getCreateTime() != null ? record.getCreateTime().toString() : null);
            vo.setUpdateTime(record.getUpdateTime() != null ? record.getUpdateTime().toString() : null);
            records.add(vo);
        }

        // 计算汇总统计（不受分页影响）
        LambdaQueryWrapper<BillRecord> summaryWrapper = new LambdaQueryWrapper<>();
        summaryWrapper.eq(BillRecord::getUserId, userId);
        if (type != null) {
            summaryWrapper.eq(BillRecord::getType, type);
        }
        if (categoryId != null) {
            summaryWrapper.eq(BillRecord::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(startDate)) {
            summaryWrapper.ge(BillRecord::getRecordTime, startDate + " 00:00:00");
        }
        if (StringUtils.hasText(endDate)) {
            summaryWrapper.le(BillRecord::getRecordTime, endDate + " 23:59:59");
        }
        if (StringUtils.hasText(keyword)) {
            summaryWrapper.like(BillRecord::getDescription, keyword);
        }
        if (minAmount != null) {
            summaryWrapper.ge(BillRecord::getAmount, minAmount);
        }
        if (maxAmount != null) {
            summaryWrapper.le(BillRecord::getAmount, maxAmount);
        }

        List<BillRecord> allRecords = billRecordMapper.selectList(summaryWrapper);
        double totalIncome = allRecords.stream().filter(r -> r.getType() == 1)
                .mapToDouble(BillRecord::getAmount).sum();
        double totalExpense = allRecords.stream().filter(r -> r.getType() == 0)
                .mapToDouble(BillRecord::getAmount).sum();

        PageResult<BillVO> pageResult = new PageResult<>(result.getTotal(), page, size, records);

        Map<String, Object> response = new HashMap<>();
        response.put("total", pageResult.getTotal());
        response.put("page", pageResult.getPage());
        response.put("size", pageResult.getSize());
        response.put("pages", pageResult.getPages());
        response.put("records", pageResult.getRecords());
        Map<String, Double> summary = new HashMap<>();
        summary.put("totalIncome", totalIncome);
        summary.put("totalExpense", totalExpense);
        response.put("summary", summary);
        return response;
    }

    @Override
    public Object getBillDetail(Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        BillRecord record = billRecordMapper.selectOne(
                new LambdaQueryWrapper<BillRecord>()
                        .eq(BillRecord::getId, id)
                        .eq(BillRecord::getUserId, userId));
        if (record == null) {
            throw new BusinessException(404, "账单不存在");
        }
        BillCategory category = billCategoryMapper.selectById(record.getCategoryId());
        BillVO vo = new BillVO();
        vo.setId(record.getId());
        vo.setType(record.getType());
        vo.setAmount(record.getAmount());
        vo.setCategoryId(record.getCategoryId());
        vo.setCategoryName(category != null ? category.getName() : null);
        vo.setCategoryIcon(category != null ? category.getIcon() : null);
        vo.setDescription(record.getDescription());
        vo.setRecordTime(record.getRecordTime() != null ? record.getRecordTime().toString() : null);
        vo.setCreateTime(record.getCreateTime() != null ? record.getCreateTime().toString() : null);
        vo.setUpdateTime(record.getUpdateTime() != null ? record.getUpdateTime().toString() : null);
        return vo;
    }

    @Override
    public Object updateBill(Long id, BillUpdateRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        BillRecord record = billRecordMapper.selectOne(
                new LambdaQueryWrapper<BillRecord>()
                        .eq(BillRecord::getId, id)
                        .eq(BillRecord::getUserId, userId));
        if (record == null) {
            throw new BusinessException(404, "账单不存在");
        }
        // 校验分类
        if (request.getCategoryId() != null) {
            BillCategory category = billCategoryMapper.selectById(request.getCategoryId());
            if (category == null || category.getStatus() == 1) {
                throw new BusinessException(400, "分类不存在或已禁用");
            }
            if (!category.getType().equals(record.getType())) {
                throw new BusinessException(400, "选择的分类与账单类型不匹配");
            }
            record.setCategoryId(request.getCategoryId());
        }
        if (request.getAmount() != null) {
            record.setAmount(request.getAmount());
        }
        if (request.getDescription() != null) {
            record.setDescription(request.getDescription());
        }
        if (StringUtils.hasText(request.getRecordTime())) {
            record.setRecordTime(LocalDateTime.parse(request.getRecordTime(),
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        billRecordMapper.updateById(record);

        BillCategory category = billCategoryMapper.selectById(record.getCategoryId());
        BillVO vo = new BillVO();
        vo.setId(record.getId());
        vo.setType(record.getType());
        vo.setAmount(record.getAmount());
        vo.setCategoryId(record.getCategoryId());
        vo.setCategoryName(category != null ? category.getName() : null);
        vo.setCategoryIcon(category != null ? category.getIcon() : null);
        vo.setDescription(record.getDescription());
        vo.setRecordTime(record.getRecordTime() != null ? record.getRecordTime().toString() : null);
        vo.setUpdateTime(record.getUpdateTime() != null ? record.getUpdateTime().toString() : null);
        return vo;
    }

    @Override
    public void deleteBill(Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        BillRecord record = billRecordMapper.selectOne(
                new LambdaQueryWrapper<BillRecord>()
                        .eq(BillRecord::getId, id)
                        .eq(BillRecord::getUserId, userId));
        if (record == null) {
            throw new BusinessException(404, "账单不存在");
        }
        billRecordMapper.deleteById(id);
    }

    @Override
    public int batchDeleteBills(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        Long userId = SecurityUtil.getCurrentUserId();
        int count = 0;
        for (Long id : ids) {
            BillRecord record = billRecordMapper.selectOne(
                    new LambdaQueryWrapper<BillRecord>()
                            .eq(BillRecord::getId, id)
                            .eq(BillRecord::getUserId, userId));
            if (record != null) {
                billRecordMapper.deleteById(id);
                count++;
            }
        }
        return count;
    }
}
