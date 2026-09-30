package com.apimock.interceptor;

import com.apimock.model.entity.MockApi;
import com.apimock.service.IpWhitelistService;
import com.apimock.service.MockEngineService;
import com.apimock.util.IpUtils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Component
public class MockRequestInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(MockRequestInterceptor.class);

    private final MockEngineService mockEngineService;
    private final IpWhitelistService ipWhitelistService;

    public MockRequestInterceptor(MockEngineService mockEngineService, IpWhitelistService ipWhitelistService) {
        this.mockEngineService = mockEngineService;
        this.ipWhitelistService = ipWhitelistService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String method = request.getMethod();
        String path = request.getRequestURI();

        log.debug("拦截请求: {} {}", method, path);

        // IP 白名单校验（开关在 application.yml：apimock.ip-whitelist.enabled）
        // 本机回环地址始终放行，防止把自己锁在外面
        if (ipWhitelistService.isEnabled()) {
            String clientIp = request.getRemoteAddr();
            if (!IpUtils.isLoopback(clientIp) && !ipWhitelistService.isAllowed(clientIp)) {
                log.warn("IP 白名单拦截: {} -> {} {}", clientIp, method, path);
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\":\"Forbidden\"}");
                return false;
            }
        }

        // 缓存请求体（InputStream 只能读一次，提前缓存供后续使用）
        cacheRequestBody(request);

        // 匹配 Mock 接口
        MockApi mockApi = mockEngineService.matchMockApi(method, path);

        if (mockApi == null) {
            // 未匹配，记录并返回 404
            log.info("未匹配到 Mock 规则: {} {}", method, path);
            mockEngineService.recordRequest(request, null, 404, "{\"error\":\"No mock rule matched\"}", false);
            response.setStatus(404);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"No mock rule matched\",\"path\":\"" + path + "\"}");
            return false;
        }

        log.info("匹配到 Mock 规则 [{}]: {} {} -> type={}", mockApi.getId(), method, path,
                mockApi.getResponseType() != null ? mockApi.getResponseType() : "static");

        // 构建并返回响应（传入 request 以支持动态响应和文件流）
        mockEngineService.buildResponse(mockApi, request, response);

        // 记录请求
        mockEngineService.recordRequest(request, mockApi,
                mockApi.getResponseStatus() != null ? mockApi.getResponseStatus() : 200,
                mockApi.getResponseBody(), true);

        return false; // 不再继续处理
    }

    /**
     * 缓存请求体到 request attribute，避免 InputStream 被多次读取
     */
    private void cacheRequestBody(HttpServletRequest request) {
        try {
            String contentType = request.getContentType();
            if (contentType != null && contentType.toLowerCase().contains("multipart")) {
                return; // 文件上传不缓存
            }
            BufferedReader reader = request.getReader();
            String body = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            if (!body.isEmpty()) {
                request.setAttribute("_request_body_raw", body);
                // 重新包装 request 以便后续读取（通过 attribute 传递，不需要重新包装）
            }
        } catch (IOException e) {
            log.debug("缓存请求体失败: {}", e.getMessage());
        }
    }
}
