package com.finance.modules.memo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("memo")
public class Memo {

    @TableId(type = IdType.AUTO)
    private Long id;//备忘录id

    private Long userId;//用户id

    private String title;//标题

    private String content;//内容

    private LocalDateTime remindTime;//提醒时间

    private Integer isCompleted;//是否完成：0-未完成, 1-已完成

    private LocalDateTime createTime;//创建时间

    private LocalDateTime updateTime;//更新时间
}
