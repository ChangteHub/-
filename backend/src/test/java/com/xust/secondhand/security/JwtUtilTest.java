package com.xust.secondhand.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JwtUtil 单元测试（不依赖 Spring 容器）
 */
class JwtUtilTest {

    private static final String TEST_KEY = "unit-test-jwt-secret-with-at-least-32-bytes!!";
    private static final long EXPIRATION = 86_400_000L;

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_KEY);
        ReflectionTestUtils.setField(jwtUtil, "expiration", EXPIRATION);
    }

    @Test
    @DisplayName("生成的 Token 能解析出 userId/username/role")
    void generateThenParse() {
        String token = jwtUtil.generateToken(42L, "test1", 1);

        assertEquals(42L, jwtUtil.getUserIdFromToken(token));
        assertEquals("test1", jwtUtil.getUsernameFromToken(token));
        assertEquals(1, jwtUtil.getRoleFromToken(token));
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    @DisplayName("无效 Token 校验返回 false 而不是抛异常")
    void invalidToken() {
        assertFalse(jwtUtil.validateToken("not-a-jwt"));
        assertFalse(jwtUtil.validateToken(""));
    }

    @Test
    @DisplayName("用不同密钥签发的 Token 校验失败")
    void wrongSecretToken() {
        String token = jwtUtil.generateToken(1L, "test1", 0);

        JwtUtil other = new JwtUtil();
        ReflectionTestUtils.setField(other, "secret", "another-secret-key-with-32-bytes-at-least!!!");
        ReflectionTestUtils.setField(other, "expiration", EXPIRATION);

        assertFalse(other.validateToken(token));
    }

    @Test
    @DisplayName("密钥不足 32 字节时启动校验失败")
    void weakSigningKeyRejected() {
        JwtUtil weak = new JwtUtil();
        ReflectionTestUtils.setField(weak, "secret", "short-secret");
        ReflectionTestUtils.setField(weak, "expiration", EXPIRATION);

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, weak::checkSecretStrength);
    }
}
