package com.finance.modules.bill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("bill_category")
public class BillCategory {

    @TableId(type = IdType.AUTO)
    private Long id;//分类id

    private String name;//分类名

    private Integer type;//类型：0-支出, 1-收入

    private String icon;//图标标识

    private Integer sortOrder;//排序序号

    private Integer isDefault;//是否系统预设：0-否, 1-是

    private Integer status;//状态：0-启用, 1-禁用

    private LocalDateTime createTime;//创建时间

    private LocalDateTime updateTime;//更新时间
}
