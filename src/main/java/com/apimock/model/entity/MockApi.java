package com.apimock.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MockApi {

    private Long id;
    private Long projectId;
    private String name;
    private String method;
    private String path;
    private Integer enabled;
    private Integer responseStatus;
    private String responseHeaders;
    private String responseBody;
    private String responseType;       // 'static' | 'dynamic' | 'file'
    private String responseFilePath;   // 文件流模式：文件路径（UUID 重命名后）
    private String responseFileName;   // 文件流模式：原始文件名
    private String responseContentType; // 文件流模式：Content-Type
    private String dynamicRules;       // 动态响应规则（JSON 数组）
    private Integer delayMs;
    private String errorType;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
