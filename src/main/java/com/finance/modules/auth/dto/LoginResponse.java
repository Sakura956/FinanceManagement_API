package com.finance.modules.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应 DTO
 * 封装登录成功后返回的数据结构：token、userInfo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;

    private UserInfo userInfo;

    //内部类：UserInfo
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String phone;
        private String nickname;
        private String role;
        private String avatarUrl;
    }
}
