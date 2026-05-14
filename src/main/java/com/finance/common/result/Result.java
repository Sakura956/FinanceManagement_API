package com.finance.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用返回类型
 * @param <T>
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    private int code;
    private String message;
    private T data;

    /**
     * 成功，无数据
     * @return
     * @param <T>
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    /**
     * 成功，带数据（最常用）
     * @param data
     * @return
     * @param <T>
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /**
     * 成功，自定义提示 + 数据
     * @param message
     * @param data
     * @return
     * @param <T>
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    /**
     * 错误，自定义码 + 消息
     * @param code
     * @param message
     * @return
     * @param <T>
     */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 错误，码 + 消息 + 数据
     * @param code
     * @param message
     * @param data
     * @return
     * @param <T>
     */
    public static <T> Result<T> error(int code, String message, T data) {
        return new Result<>(code, message, data);
    }

}
