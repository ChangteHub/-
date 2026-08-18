package com.xust.secondhand.controller;

import com.xust.secondhand.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@Slf4j
@Tag(name = "文件上传接口", description = "图片上传")
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Value("${upload.path:./uploads/}")
    private String uploadPath;

    @Value("${upload.url-prefix:http://localhost:8080/uploads/}")
    private String urlPrefix;

    // 允许的图片扩展名
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp");

    // 允许的MIME类型
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp"
    );

    @Operation(summary = "上传图片")
    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.badRequest("文件不能为空");
        }

        // 检查MIME类型
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            return Result.badRequest("只能上传JPG/PNG/GIF/WEBP格式的图片");
        }

        // 检查文件扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            return Result.badRequest("不支持的文件扩展名");
        }

        // 检查文件大小（10MB）
        if (file.getSize() > 10 * 1024 * 1024) {
            return Result.badRequest("文件大小不能超过10MB");
        }

        try {
            // 检查文件头（Magic Number）
            byte[] fileBytes = file.getBytes();
            if (!isValidImageHeader(fileBytes)) {
                return Result.badRequest("文件内容与扩展名不匹配");
            }

            // 获取绝对路径
            Path uploadDir = Paths.get(uploadPath).toAbsolutePath();
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 生成唯一文件名
            String filename = UUID.randomUUID().toString() + extension;

            // 保存文件
            Path destFile = uploadDir.resolve(filename);
            file.transferTo(destFile.toFile());

            // 返回URL
            Map<String, String> result = new HashMap<>();
            result.put("url", urlPrefix + filename);

            return Result.success(result);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败，请重试");
        }
    }

    /**
     * 验证图片文件头（Magic Number）
     */
    private boolean isValidImageHeader(byte[] bytes) {
        if (bytes == null || bytes.length < 4) {
            return false;
        }

        // JPEG: FF D8 FF
        if (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8 && bytes[2] == (byte) 0xFF) {
            return true;
        }

        // PNG: 89 50 4E 47
        if (bytes[0] == (byte) 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47) {
            return true;
        }

        // GIF: 47 49 46 38
        if (bytes[0] == 0x47 && bytes[1] == 0x49 && bytes[2] == 0x46 && bytes[3] == 0x38) {
            return true;
        }

        // WEBP: 52 49 46 46
        if (bytes[0] == 0x52 && bytes[1] == 0x49 && bytes[2] == 0x46 && bytes[3] == 0x46) {
            return true;
        }

        // BMP: 42 4D
        if (bytes[0] == 0x42 && bytes[1] == 0x4D) {
            return true;
        }

        return false;
    }
}
