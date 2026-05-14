package com.finance.modules.admin.service;

import com.finance.modules.admin.dto.UserStatusRequest;
import com.finance.common.result.PageResult;

public interface AdminUserService {

    PageResult<?> listUsers(int page, int size, String keyword, Integer status,
                            String startDate, String endDate);

    Object getUserDetail(Long userId);

    void updateUserStatus(Long userId, UserStatusRequest request);
}
