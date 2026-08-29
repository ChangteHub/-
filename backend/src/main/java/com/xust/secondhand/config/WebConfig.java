package com.xust.secondhand.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 静态资源配置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${upload.path:./uploads/}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将路径转为绝对路径，确保Windows/Linux兼容
        Path absolutePath = Paths.get(uploadPath).toAbsolutePath().normalize();
        String location = "file:" + absolutePath.toString().replace("\\", "/") + "/";

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}
