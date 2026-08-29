package com.xust.secondhand.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xust.secondhand.dto.request.LoginRequest;
import com.xust.secondhand.dto.response.LoginResponse;
import com.xust.secondhand.dto.response.UserResponse;
import com.xust.secondhand.service.UserService;
import com.xust.secondhand.repository.UserRepository;
import com.xust.secondhand.security.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 认证接口 MockMvc 测试（Slice 测试，不加载 SecurityConfig 与数据库）
 */
@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    // JwtAuthenticationFilter 是 servlet Filter，会被 WebMvcTest 切片装配；
    // 其依赖 JwtUtil/UserRepository 需在此模拟（addFilters=false，过滤器逻辑不参与断言）
    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserRepository userRepository;

    @Test
    @DisplayName("登录成功返回统一 Result 结构与 token")
    void loginSuccess() throws Exception {
        UserResponse user = new UserResponse();
        user.setId(1L);
        user.setUsername("test1");
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken("fake-jwt-token");
        loginResponse.setUser(user);
        when(userService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        LoginRequest request = new LoginRequest();
        request.setUsername("test1");
        request.setPassword("115417");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("fake-jwt-token"))
                .andExpect(jsonPath("$.data.user.username").value("test1"));
    }

    @Test
    @DisplayName("登录参数缺失返回 400 与统一错误结构")
    void loginValidationFailure() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
}
