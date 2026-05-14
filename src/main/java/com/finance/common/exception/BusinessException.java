package com.finance.common.exception;

/**
 * 业务异常，携带code+message可在 Service 层抛出，最后GlobalExceptionHandler 捕获
 * 能自定义 code
 */
public class BusinessException extends RuntimeException {

    private final int code;// 自定义错误码


    /**
     * 传 码 + 消息
     *
     * @param code
     * @param message
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }


    /**
     * 只传消息，默认码 400
     *
     * @param message
     */
    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }


    /**
     * 获取错误码
     * 给全局异常处理器使用的
     *
     * @return
     */
    public int getCode() {
        return code;
    }
}
