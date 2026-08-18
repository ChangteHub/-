package com.xust.secondhand.common;

import lombok.Data;

/**
 * 统一返回结果
 */
@Data
public class Result<T> {

    private int code;
    private String message;
    private T data;

    public Result() {}

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功返回
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    /**
     * 失败返回
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 未授权
     */
    public static <T> Result<T> unauthorized(String message) {
        return new Result<>(401, message, null);
    }

    /**
     * 禁止访问
     */
    public static <T> Result<T> forbidden(String message) {
        return new Result<>(403, message, null);
    }

    /**
     * 参数错误
     */
    public static <T> Result<T> badRequest(String message) {
        return new Result<>(400, message, null);
    }
}
