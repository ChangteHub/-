package com.xust.secondhand.config;

import com.xust.secondhand.utils.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security配置
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000,http://localhost:8080}")
    private String allowedOrigins;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF
            .csrf(AbstractHttpConfigurer::disable)
            // 启用CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // 无状态Session
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 未认证请求统一返回401 JSON（与前端拦截器语义对齐）
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(unauthorizedEntryPoint())
                    .accessDeniedHandler(accessDeniedHandler()))
            // 请求授权
            .authorizeHttpRequests(auth -> auth
                // 认证接口（公开）
                .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                // 管理员接口（需要ADMIN角色）
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // 公开查询接口
                .requestMatchers(HttpMethod.GET, "/api/category/**", "/api/product/list", "/api/product/{id}").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/banners", "/api/help", "/api/search/**").permitAll()
                // 文件上传（需要认证）
                .requestMatchers("/api/upload/**").authenticated()
                // 静态资源
                .requestMatchers("/uploads/**", "/avatars/**", "/products/**", "/icons/**").permitAll()
                // Swagger/Knife4j
                .requestMatchers("/doc.html", "/swagger-ui/**", "/v3/api-docs/**", "/webjars/**", "/favicon.ico").permitAll()
                // WebSocket
                .requestMatchers("/ws/**").permitAll()
                // H2控制台（生产环境无需，已移除放行）
                // OPTIONS请求
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // 其他需要认证
                .anyRequest().authenticated()
            )
            // 添加JWT过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList());
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 未认证请求统一返回 401 JSON（与前端拦截器语义对齐）
     */
    @Bean
    public AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期\",\"data\":null}");
        };
    }

    /**
     * 已认证但权限不足的请求统一返回 403 JSON（与前端拦截器语义对齐）
     */
    @Bean
    public org.springframework.security.web.access.AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"message\":\"没有权限执行此操作\",\"data\":null}");
        };
    }
}
