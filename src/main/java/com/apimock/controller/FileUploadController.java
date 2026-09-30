package com.apimock.controller;

import com.apimock.model.dto.MockResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class FileUploadController {

    private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);

    @Value("${apimock.upload-dir:./data/files}")
    private String uploadDir;

    /**
     * 解析上传目录为绝对路径
     */
    private File getUploadDir() {
        File dir = new File(uploadDir);
        if (!dir.isAbsolute()) {
            dir = new File(System.getProperty("user.dir"), uploadDir);
        }
        return dir;
    }

    @PostMapping("/upload")
    public MockResponse<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return MockResponse.error(400, "文件不能为空");
        }

        try {
            // 确保上传目录存在（使用绝对路径）
            File dir = getUploadDir();
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成唯一文件名，保留原始扩展名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + extension;

            // 保存文件
            File dest = new File(dir, newFilename);
            file.transferTo(dest);

            // 返回文件信息
            Map<String, String> result = new HashMap<>();
            result.put("filePath", dest.getAbsolutePath());
            result.put("fileName", originalFilename != null ? originalFilename : newFilename);
            result.put("contentType", file.getContentType() != null ? file.getContentType() : "");
            result.put("size", String.valueOf(file.getSize()));

            log.info("文件上传成功: {} -> {}", originalFilename, dest.getAbsolutePath());
            return MockResponse.ok(result);

        } catch (IOException e) {
            log.error("文件上传失败", e);
            return MockResponse.error(500, "文件上传失败: " + e.getMessage());
        }
    }
}
