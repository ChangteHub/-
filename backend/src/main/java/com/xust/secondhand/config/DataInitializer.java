package com.xust.secondhand.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xust.secondhand.entity.User;
import com.xust.secondhand.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * 数据初始化器 - 自动创建管理员账号
 *
 * 安全说明：
 * - 管理员初始密码优先取环境变量 ADMIN_PASSWORD；
 * - 未设置时随机生成并仅打印一次（日志），生产环境建议设置 ADMIN_PASSWORD 并首次登录后修改；
 * - 不再硬编码默认密码。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.password:}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        initAdminUser();
    }

    private void initAdminUser() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, 1);
        Long adminCount = userRepository.selectCount(wrapper);

        if (adminCount > 0) {
            return;
        }

        log.info("未发现管理员账号，正在创建默认管理员...");
        String password = resolveAdminPassword();

        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode(password));
        admin.setNickname("系统管理员");
        admin.setSchool("西南科技大学");
        admin.setStudentId("ADMIN001");
        admin.setBio("平台管理员");
        admin.setStatus(0);
        admin.setRole(1);
        try {
            userRepository.insert(admin);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            // 极端情况：admin 用户名已被普通用户占用（如历史数据），阻止启动失败
            log.error("管理员账号创建失败：用户名 admin 已被其他账号占用，请手动处理后再启动");
            return;
        }

        // 密码仅打印一次：来源为环境变量时提示已配置；随机生成时提示立即修改
        if (adminPassword != null && !adminPassword.isEmpty()) {
            log.info("管理员账号已创建：用户名 admin（密码来自 ADMIN_PASSWORD 环境变量），请妥善保管");
        } else {
            log.warn("管理员账号已创建：用户名 admin，初始密码为随机生成的一次性密码：{}（请立即登录并修改密码）", password);
        }
    }

    /**
     * 解析管理员初始密码：优先环境变量，否则随机生成
     */
    private String resolveAdminPassword() {
        if (adminPassword != null && !adminPassword.isEmpty()) {
            return adminPassword;
        }
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
