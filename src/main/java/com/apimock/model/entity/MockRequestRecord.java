package com.apimock.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MockRequestRecord {

    private Long id;
    private Long projectId;
    private Long mockApiId;
    private String method;
    private String path;
    private String clientIp;
    private String requestHeaders;
    private String requestBody;
    private String queryParams;
    private Integer responseStatus;
    private String responseBody;
    private Integer matched;
    private LocalDateTime createdAt;
}
