package com.finance.modules.bill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("bill_record")
public class BillRecord {

    @TableId(type = IdType.AUTO)
    private Long id;//记录id

    private Long userId;//用户id

    private Integer type;//类型：0-支出, 1-收入

    private Double amount;//金额

    private Long categoryId;//分类id

    private String description;//备注描述

    private LocalDateTime recordTime;//记录时间（用户实际消费/收入时间）

    private LocalDateTime createTime;//创建时间

    private LocalDateTime updateTime;//更新时间
}
