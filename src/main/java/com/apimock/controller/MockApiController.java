package com.apimock.controller;

import com.apimock.model.dto.MockApiDTO;
import com.apimock.model.dto.MockResponse;
import com.apimock.model.entity.MockApi;
import com.apimock.service.MockApiService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/mock-apis")
public class MockApiController {

    private final MockApiService mockApiService;

    public MockApiController(MockApiService mockApiService) {
        this.mockApiService = mockApiService;
    }

    @GetMapping
    public MockResponse<List<MockApi>> list(@RequestParam(required = false) Long projectId) {
        if (projectId != null) {
            return MockResponse.ok(mockApiService.findByProjectId(projectId));
        }
        return MockResponse.ok(mockApiService.findAll());
    }

    @GetMapping("/{id}")
    public MockResponse<MockApi> getById(@PathVariable Long id) {
        MockApi api = mockApiService.findById(id);
        if (api == null) {
            return MockResponse.error(404, "Mock API not found");
        }
        return MockResponse.ok(api);
    }

    @PostMapping
    public MockResponse<MockApi> create(@RequestBody MockApiDTO dto) {
        try {
            MockApi api = mockApiService.create(dto);
            return MockResponse.ok(api);
        } catch (Exception e) {
            return MockResponse.error(400, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public MockResponse<MockApi> update(@PathVariable Long id, @RequestBody MockApiDTO dto) {
        try {
            MockApi api = mockApiService.update(id, dto);
            return MockResponse.ok(api);
        } catch (Exception e) {
            return MockResponse.error(400, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public MockResponse<Void> delete(@PathVariable Long id) {
        try {
            mockApiService.delete(id);
            return MockResponse.ok(null);
        } catch (Exception e) {
            return MockResponse.error(400, e.getMessage());
        }
    }

    @PatchMapping("/{id}/toggle")
    public MockResponse<MockApi> toggle(@PathVariable Long id) {
        try {
            MockApi api = mockApiService.toggle(id);
            return MockResponse.ok(api);
        } catch (Exception e) {
            return MockResponse.error(404, e.getMessage());
        }
    }
}
