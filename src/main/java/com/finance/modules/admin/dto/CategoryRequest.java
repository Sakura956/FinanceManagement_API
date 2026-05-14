package com.finance.modules.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRequest {

    @NotBlank(message = "分类名称不能为空")
    @Size(min = 2, max = 10, message = "分类名称2-10个字符")
    private String name;

    @NotNull(message = "分类类型不能为空")
    private Integer type;

    private String icon;

    @Min(0)
    private Integer sortOrder;
}
