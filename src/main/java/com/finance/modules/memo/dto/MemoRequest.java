package com.finance.modules.memo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MemoRequest {

    @NotBlank(message = "标题不能为空")
    @Size(min = 1, max = 100, message = "标题1-100字")
    private String title;

    private String content;

    private String remindTime;
}
