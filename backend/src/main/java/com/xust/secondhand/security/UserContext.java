package com.xust.secondhand.security;

/**
 * 用户上下文（基于ThreadLocal存储当前登录用户ID和角色）
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<Integer> ROLE = new ThreadLocal<>();

    /**
     * 设置当前用户ID
     */
    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    /**
     * 获取当前用户ID
     */
    public static Long getUserId() {
        return USER_ID.get();
    }

    /**
     * 设置当前用户角色
     */
    public static void setRole(Integer role) {
        ROLE.set(role);
    }

    /**
     * 获取当前用户角色
     */
    public static Integer getRole() {
        Integer role = ROLE.get();
        return role != null ? role : 0;
    }

    /**
     * 判断当前用户是否是管理员
     */
    public static boolean isAdmin() {
        return getRole() == 1;
    }

    /**
     * 清除当前用户信息
     */
    public static void clear() {
        USER_ID.remove();
        ROLE.remove();
    }
}
