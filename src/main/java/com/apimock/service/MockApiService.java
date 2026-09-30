package com.apimock.service;

import com.apimock.mapper.MockApiMapper;
import com.apimock.model.dto.MockApiDTO;
import com.apimock.model.entity.MockApi;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MockApiService {

    private final MockApiMapper mockApiMapper;

    public MockApiService(MockApiMapper mockApiMapper) {
        this.mockApiMapper = mockApiMapper;
    }

    public List<MockApi> findAll() {
        return mockApiMapper.findAll();
    }

    public List<MockApi> findByProjectId(Long projectId) {
        return mockApiMapper.findByProjectId(projectId);
    }

    public MockApi findById(Long id) {
        return mockApiMapper.findById(id);
    }

    public MockApi findByMethodAndPath(String method, String path) {
        return mockApiMapper.findByMethodAndPath(method, path);
    }

    public MockApi create(MockApiDTO dto) {
        MockApi api = new MockApi();
        api.setProjectId(dto.getProjectId() != null ? dto.getProjectId() : 1L);
        api.setName(dto.getName());
        api.setMethod(dto.getMethod().toUpperCase());
        api.setPath(dto.getPath());
        api.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : 1);
        api.setResponseStatus(dto.getResponseStatus() != null ? dto.getResponseStatus() : 200);
        api.setResponseHeaders(dto.getResponseHeaders());
        api.setResponseBody(dto.getResponseBody());
        api.setResponseType(dto.getResponseType() != null ? dto.getResponseType() : "static");
        api.setResponseFilePath(dto.getResponseFilePath());
        api.setResponseFileName(dto.getResponseFileName());
        api.setResponseContentType(dto.getResponseContentType());
        api.setDynamicRules(dto.getDynamicRules());
        api.setDelayMs(dto.getDelayMs() != null ? dto.getDelayMs() : 0);
        api.setErrorType(dto.getErrorType());
        api.setDescription(dto.getDescription());
        mockApiMapper.insert(api);
        return api;
    }

    public MockApi update(Long id, MockApiDTO dto) {
        MockApi api = mockApiMapper.findById(id);
        if (api == null) {
            throw new RuntimeException("Mock API not found: " + id);
        }
        api.setProjectId(dto.getProjectId() != null ? dto.getProjectId() : api.getProjectId());
        api.setName(dto.getName());
        api.setMethod(dto.getMethod().toUpperCase());
        api.setPath(dto.getPath());
        api.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : 1);
        api.setResponseStatus(dto.getResponseStatus() != null ? dto.getResponseStatus() : 200);
        api.setResponseHeaders(dto.getResponseHeaders());
        api.setResponseBody(dto.getResponseBody());
        api.setResponseType(dto.getResponseType() != null ? dto.getResponseType() : "static");
        api.setResponseFilePath(dto.getResponseFilePath());
        api.setResponseFileName(dto.getResponseFileName());
        api.setResponseContentType(dto.getResponseContentType());
        api.setDynamicRules(dto.getDynamicRules());
        api.setDelayMs(dto.getDelayMs() != null ? dto.getDelayMs() : 0);
        api.setErrorType(dto.getErrorType());
        api.setDescription(dto.getDescription());
        mockApiMapper.update(api);
        return api;
    }

    public void delete(Long id) {
        MockApi api = mockApiMapper.findById(id);
        if (api == null) {
            throw new RuntimeException("接口不存在: " + id);
        }
        if (api.getEnabled() != null && api.getEnabled() == 1) {
            throw new RuntimeException("接口处于启用状态，不允许删除");
        }
        mockApiMapper.deleteById(id);
    }

    public MockApi toggle(Long id) {
        MockApi api = mockApiMapper.findById(id);
        if (api == null) {
            throw new RuntimeException("Mock API not found: " + id);
        }
        int newEnabled = api.getEnabled() == 1 ? 0 : 1;
        mockApiMapper.updateEnabled(id, newEnabled);
        api.setEnabled(newEnabled);
        return api;
    }
}
