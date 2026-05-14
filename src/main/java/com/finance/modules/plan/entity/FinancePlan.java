package com.finance.modules.plan.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("finance_plan")
public class FinancePlan {

    @TableId(type = IdType.AUTO)
    private Long id;//计划id

    private Long userId;//用户id

    private String name;//名称

    private Double initialAmount;//初始投入金额

    private Double currentValue;//当前市值

    private Double expectedRoi;//预期年化收益率(%)

    private LocalDate startDate;//开始日期

    private LocalDate endDate;//结束日期

    private Integer status;//状态：0-持有中, 1-已赎回

    private String remark;//备注

    private LocalDateTime createTime;//创建时间

    private LocalDateTime updateTime;//更新时间
}
