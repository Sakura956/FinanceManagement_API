package com.finance.modules.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;//用户id

    private String phone;//手机号

    private String password;//密码

    private String nickname;//昵称

    private String role;//角色：ADMIN-管理员, USER-普通用户

    private Integer status;//状态：0-正常, 1-封禁

    private String avatarUrl;//头像地址

    private LocalDateTime createTime;//创建时间

    private LocalDateTime updateTime;//更新时间
}
