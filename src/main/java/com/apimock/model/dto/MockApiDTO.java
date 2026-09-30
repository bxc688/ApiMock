package com.apimock.model.dto;

import lombok.Data;

@Data
public class MockApiDTO {

    private Long projectId;
    private String name;
    private String method;
    private String path;
    private Integer enabled;
    private Integer responseStatus;
    private String responseHeaders;
    private String responseBody;
    private String responseType;
    private String responseFilePath;
    private String responseFileName;
    private String responseContentType;
    private String dynamicRules;
    private Integer delayMs;
    private String errorType;
    private String description;
}
