package com.apimock.service;

import com.apimock.mapper.MockApiMapper;
import com.apimock.mapper.MockRequestRecordMapper;
import com.apimock.model.entity.MockApi;
import com.apimock.model.entity.MockRequestRecord;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URLConnection;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MockEngineService {

    private static final Logger log = LoggerFactory.getLogger(MockEngineService.class);

    private final MockApiMapper mockApiMapper;
    private final MockRequestRecordMapper recordMapper;

    public MockEngineService(MockApiMapper mockApiMapper, MockRequestRecordMapper recordMapper) {
        this.mockApiMapper = mockApiMapper;
        this.recordMapper = recordMapper;
    }

    /**
     * 匹配 Mock 接口
     */
    public MockApi matchMockApi(String method, String path) {
        return mockApiMapper.findByMethodAndPath(method.toUpperCase(), path);
    }

    /**
     * 构建并写入响应（核心入口）
     */
    public void buildResponse(MockApi mockApi, HttpServletRequest request, HttpServletResponse response)
            throws IOException, InterruptedException {

        // 延迟模拟
        if (mockApi.getDelayMs() != null && mockApi.getDelayMs() > 0) {
            log.info("模拟延迟 {}ms: {} {}", mockApi.getDelayMs(), mockApi.getMethod(), mockApi.getPath());
            Thread.sleep(mockApi.getDelayMs());
        }

        // 异常模拟
        String errorType = mockApi.getErrorType();
        if (errorType != null) {
            switch (errorType.toLowerCase()) {
                case "timeout":
                    log.info("模拟超时: {} {}", mockApi.getMethod(), mockApi.getPath());
                    Thread.sleep(30000);
                    response.setStatus(504);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"error\":\"Gateway Timeout\",\"message\":\"Request timed out\"}");
                    return;
                case "500":
                    log.info("模拟500错误: {} {}", mockApi.getMethod(), mockApi.getPath());
                    response.setStatus(500);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"error\":\"Internal Server Error\",\"message\":\"Simulated server error\"}");
                    return;
                case "404":
                    log.info("模拟404错误: {} {}", mockApi.getMethod(), mockApi.getPath());
                    response.setStatus(404);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"error\":\"Not Found\",\"message\":\"Simulated not found\"}");
                    return;
                default:
                    break;
            }
        }

        // 根据响应类型分发
        String responseType = mockApi.getResponseType() != null ? mockApi.getResponseType() : "static";
        switch (responseType.toLowerCase()) {
            case "dynamic":
                buildDynamicResponse(mockApi, request, response);
                break;
            case "file":
                buildFileResponse(mockApi, response);
                break;
            case "static":
            default:
                buildStaticResponse(mockApi, response);
                break;
        }
    }

    /**
     * 静态响应（原有逻辑）
     */
    private void buildStaticResponse(MockApi mockApi, HttpServletResponse response) throws IOException {
        int status = mockApi.getResponseStatus() != null ? mockApi.getResponseStatus() : 200;
        response.setStatus(status);
        applyResponseHeaders(mockApi, response);
        if (response.getContentType() == null) {
            response.setContentType("application/json;charset=UTF-8");
        }
        String body = mockApi.getResponseBody();
        if (body != null) {
            response.getWriter().write(body);
        }
    }

    /**
     * 动态响应：根据请求参数值路由到不同的预设响应
     * 规则存储在 dynamic_rules JSON 字段中
     */
    @SuppressWarnings("unchecked")
    private void buildDynamicResponse(MockApi mockApi, HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String rulesJson = mockApi.getDynamicRules();
        if (rulesJson == null || rulesJson.trim().isEmpty()) {
            // 没有配置规则，回退到静态响应
            log.warn("动态响应未配置规则，回退到静态响应: {} {}", mockApi.getMethod(), mockApi.getPath());
            buildStaticResponse(mockApi, response);
            return;
        }

        // 解析规则
        List<Map<String, Object>> rules;
        try {
            rules = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(rulesJson, List.class);
        } catch (Exception e) {
            log.error("解析动态规则失败: {}", rulesJson, e);
            response.setStatus(500);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"Invalid dynamic rules config\"}");
            return;
        }

        // 收集请求参数（query params + request body 扁平化）
        Map<String, String> allParams = collectRequestParams(request);

        // 遍历规则，找到匹配的规则
        Map<String, Object> matchedRule = null;
        Map<String, Object> defaultRule = null;

        for (Map<String, Object> rule : rules) {
            boolean isDefault = Boolean.TRUE.equals(rule.get("isDefault"));
            if (isDefault) {
                defaultRule = rule;
                continue;
            }

            String conditionField = String.valueOf(rule.get("conditionField"));
            String conditionValue = String.valueOf(rule.get("conditionValue"));

            if (conditionField == null || conditionField.isEmpty() || "null".equals(conditionField)) {
                // 空条件字段视为默认规则
                defaultRule = rule;
                continue;
            }

            String actualValue = allParams.get(conditionField);
            if (actualValue != null && actualValue.equals(conditionValue)) {
                matchedRule = rule;
                log.info("动态规则匹配: {}={} → 返回响应", conditionField, conditionValue);
                break;
            }
        }

        // 使用匹配的规则（或默认规则）
        Map<String, Object> rule = matchedRule != null ? matchedRule : defaultRule;
        if (rule == null) {
            response.setStatus(404);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"No matching dynamic rule\"}");
            return;
        }

        // 写入响应
        int status = 200;
        if (rule.get("responseStatus") != null) {
            try {
                status = Integer.parseInt(String.valueOf(rule.get("responseStatus")));
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        response.setStatus(status);

        if (response.getContentType() == null) {
            response.setContentType("application/json;charset=UTF-8");
        }

        String responseBody = rule.get("responseBody") != null ? String.valueOf(rule.get("responseBody")) : "";
        response.getWriter().write(responseBody);
    }

    /**
     * 收集请求参数（query params + request body 扁平化）
     */
    @SuppressWarnings("unchecked")
    private Map<String, String> collectRequestParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();

        // 1. Query params
        Enumeration<String> paramNames = request.getParameterNames();
        if (paramNames != null) {
            while (paramNames.hasMoreElements()) {
                String name = paramNames.nextElement();
                params.put(name, request.getParameter(name));
            }
        }

        // 2. Request body（JSON 扁平化，只取第一层）
        try {
            Object cached = request.getAttribute("_request_body_parsed");
            if (cached instanceof Map) {
                Map<String, Object> bodyMap = (Map<String, Object>) cached;
                for (Map.Entry<String, Object> entry : bodyMap.entrySet()) {
                    Object val = entry.getValue();
                    params.put(entry.getKey(), val != null ? String.valueOf(val) : "");
                }
            } else {
                // 回退：从拦截器缓存的原始请求体字符串解析
                String rawBody = (String) request.getAttribute("_request_body_raw");
                if (rawBody != null && !rawBody.trim().isEmpty()) {
                    Map<String, Object> bodyMap = new ObjectMapper().readValue(rawBody, Map.class);
                    for (Map.Entry<String, Object> entry : bodyMap.entrySet()) {
                        Object val = entry.getValue();
                        params.put(entry.getKey(), val != null ? String.valueOf(val) : "");
                    }
                }
            }
        } catch (Exception e) {
            log.debug("收集请求体参数失败: {}", e.getMessage());
        }

        return params;
    }

    /**
     * 文件流响应
     */
    private void buildFileResponse(MockApi mockApi, HttpServletResponse response) throws IOException {
        String filePath = mockApi.getResponseFilePath();
        if (filePath == null || filePath.trim().isEmpty()) {
            response.setStatus(404);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"File path not configured\"}");
            return;
        }

        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            response.setStatus(404);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"File not found: " + filePath.replace("\"", "\\\"") + "\"}");
            return;
        }

        int status = mockApi.getResponseStatus() != null ? mockApi.getResponseStatus() : 200;
        response.setStatus(status);

        // 设置 Content-Type
        String contentType = mockApi.getResponseContentType();
        if (contentType == null || contentType.trim().isEmpty()) {
            contentType = URLConnection.guessContentTypeFromName(file.getName());
        }
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        response.setContentType(contentType);
        response.setContentLengthLong(file.length());
        response.setHeader("Content-Disposition", "inline; filename=\"" + file.getName() + "\"");

        // 流式写入
        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
        }
    }

    /**
     * 应用自定义响应头
     */
    private void applyResponseHeaders(MockApi mockApi, HttpServletResponse response) {
        if (mockApi.getResponseHeaders() != null && !mockApi.getResponseHeaders().trim().isEmpty()) {
            try {
                Map<String, String> headers = new com.fasterxml.jackson.databind.ObjectMapper()
                        .readValue(mockApi.getResponseHeaders(),
                                new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {});
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    response.setHeader(entry.getKey(), entry.getValue());
                }
            } catch (Exception e) {
                log.warn("解析响应头失败: {}", mockApi.getResponseHeaders(), e);
            }
        }
    }

    /**
     * 记录请求（增强版：收集 request body）
     */
    public void recordRequest(HttpServletRequest request, MockApi matchedApi,
                              int responseStatus, String responseBody, boolean matched) {
        try {
            MockRequestRecord record = new MockRequestRecord();
            record.setProjectId(matchedApi != null ? matchedApi.getProjectId() : 1L);
            record.setMockApiId(matchedApi != null ? matchedApi.getId() : null);
            record.setMethod(request.getMethod());
            record.setPath(request.getRequestURI());
            record.setClientIp(getClientIp(request));
            record.setMatched(matched ? 1 : 0);
            record.setResponseStatus(responseStatus);
            record.setResponseBody(responseBody);

            // 收集请求头
            Map<String, String> headers = new HashMap<>();
            Enumeration<String> headerNames = request.getHeaderNames();
            if (headerNames != null) {
                while (headerNames.hasMoreElements()) {
                    String name = headerNames.nextElement();
                    headers.put(name, request.getHeader(name));
                }
            }
            record.setRequestHeaders(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(headers));

            // 收集查询参数
            Map<String, String[]> paramMap = request.getParameterMap();
            if (!paramMap.isEmpty()) {
                record.setQueryParams(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(paramMap));
            }

            // 收集请求体
            Object cachedBody = request.getAttribute("_request_body_raw");
            if (cachedBody instanceof String) {
                record.setRequestBody((String) cachedBody);
            }

            recordMapper.insert(record);
        } catch (Exception e) {
            log.error("记录请求失败", e);
        }
    }

    /**
     * 获取客户端真实 IP（支持代理）
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
