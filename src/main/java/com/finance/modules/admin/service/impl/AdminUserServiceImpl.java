package com.finance.modules.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.finance.common.exception.BusinessException;
import com.finance.common.result.PageResult;
import com.finance.modules.admin.dto.UserStatusRequest;
import com.finance.modules.admin.service.AdminUserService;
import com.finance.modules.auth.entity.User;
import com.finance.modules.auth.mapper.UserMapper;
import com.finance.modules.bill.mapper.BillRecordMapper;
import com.finance.modules.memo.mapper.MemoMapper;
import com.finance.modules.plan.mapper.FinancePlanMapper;
import com.finance.util.RedisUtil;
import com.finance.util.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    private final UserMapper userMapper;
    private final BillRecordMapper billRecordMapper;
    private final MemoMapper memoMapper;
    private final FinancePlanMapper financePlanMapper;
    private final RedisUtil redisUtil;

    public AdminUserServiceImpl(UserMapper userMapper, BillRecordMapper billRecordMapper,
                                 MemoMapper memoMapper, FinancePlanMapper financePlanMapper,
                                 RedisUtil redisUtil) {
        this.userMapper = userMapper;
        this.billRecordMapper = billRecordMapper;
        this.memoMapper = memoMapper;
        this.financePlanMapper = financePlanMapper;
        this.redisUtil = redisUtil;
    }

    /**
     * 分页查询用户列表
     * @param page
     * @param size
     * @param keyword
     * @param status
     * @param startDate
     * @param endDate
     * @return
     */
    @Override
    public PageResult<?> listUsers(int page, int size, String keyword, Integer status,
                                   String startDate, String endDate) {
        //MyBatis-Plus 条件构造器，用 Lambda 语法 构建 SQL 条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getPhone, keyword).or().like(User::getNickname, keyword));
        }

        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }

        if (StringUtils.hasText(startDate)) {
            wrapper.ge(User::getCreateTime, startDate + " 00:00:00");
        }

        if (StringUtils.hasText(endDate)) {
            wrapper.le(User::getCreateTime, endDate + " 23:59:59");
        }
        //按 创建时间倒序
        wrapper.orderByDesc(User::getCreateTime);

        //执行分页查询
        Page<User> result = userMapper.selectPage(new Page<>(page, size), wrapper);
        // 脱敏，不返回密码
        result.getRecords().forEach(u -> u.setPassword(null));

        return new PageResult<>(result.getTotal(), page, size, result.getRecords());
    }

    /**
     * 获取用户详细信息
     * @param userId
     * @return
     */
    @Override
    public Object getUserDetail(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setPassword(null);

        // 统计用户数据
        Long billCount = billRecordMapper.selectCount(
                new LambdaQueryWrapper<com.finance.modules.bill.entity.BillRecord>()
                        .eq(com.finance.modules.bill.entity.BillRecord::getUserId, userId));
        Long memoCount = memoMapper.selectCount(
                new LambdaQueryWrapper<com.finance.modules.memo.entity.Memo>()
                        .eq(com.finance.modules.memo.entity.Memo::getUserId, userId));
        Long planCount = financePlanMapper.selectCount(
                new LambdaQueryWrapper<com.finance.modules.plan.entity.FinancePlan>()
                        .eq(com.finance.modules.plan.entity.FinancePlan::getUserId, userId));

        Map<String, Object> detail = new HashMap<>();
        detail.put("id", user.getId());
        detail.put("phone", user.getPhone());
        detail.put("nickname", user.getNickname());
        detail.put("role", user.getRole());
        detail.put("status", user.getStatus());
        detail.put("avatarUrl", user.getAvatarUrl());
        detail.put("billCount", billCount);
        detail.put("memoCount", memoCount);
        detail.put("planCount", planCount);
        detail.put("createTime", user.getCreateTime());
        detail.put("updateTime", user.getUpdateTime());
        return detail;
    }

    /**
     * 修改用户状态（封禁/解封）
     * @param userId
     * @param request
     */
    @Override
    public void updateUserStatus(Long userId, UserStatusRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId.equals(userId)) {
            throw new BusinessException(400, "不能封禁自己的账号");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setStatus(request.getStatus());
        userMapper.updateById(user);
        // 若封禁，清除Redis中的Token，强制下线
        if (request.getStatus() != null && request.getStatus() == 1) {
            redisUtil.delete("token:user:" + userId);
        }
    }
}
