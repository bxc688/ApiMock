package com.apimock.controller;

import com.apimock.model.dto.MockResponse;
import com.apimock.model.entity.MockRequestRecord;
import com.apimock.service.MockRequestRecordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/records")
public class MockRequestRecordController {

    private final MockRequestRecordService recordService;

    public MockRequestRecordController(MockRequestRecordService recordService) {
        this.recordService = recordService;
    }

    @GetMapping
    public MockResponse<Map<String, Object>> list(
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(required = false) Long mockApiId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String searchPath,
            @RequestParam(required = false) String searchName) {
        List<MockRequestRecord> records;
        int total;
        if (searchPath != null && !searchPath.trim().isEmpty()) {
            records = recordService.searchByPath(searchPath.trim(), limit, offset);
            total = recordService.countByPath(searchPath.trim());
        } else if (searchName != null && !searchName.trim().isEmpty()) {
            records = recordService.searchByApiName(searchName.trim(), limit, offset);
            total = recordService.countByApiName(searchName.trim());
        } else if (mockApiId != null) {
            records = recordService.findByMockApiId(mockApiId, limit, offset);
            total = records.size();
        } else if (projectId != null) {
            records = recordService.findByProjectId(projectId, limit, offset);
            total = recordService.countByProjectId(projectId);
        } else {
            records = recordService.findAll(limit, offset);
            total = recordService.countAll();
        }
        Map<String, Object> result = new HashMap<>();
        result.put("data", records);
        result.put("total", total);
        return MockResponse.ok(result);
    }

    @GetMapping("/{id}")
    public MockResponse<MockRequestRecord> getById(@PathVariable Long id) {
        MockRequestRecord record = recordService.findById(id);
        if (record == null) {
            return MockResponse.error(404, "Record not found");
        }
        return MockResponse.ok(record);
    }

    @DeleteMapping
    public ResponseEntity<Void> clear(
            @RequestParam(required = false) Long mockApiId,
            @RequestParam(required = false) Long projectId) {
        if (mockApiId != null) {
            recordService.deleteByMockApiId(mockApiId);
        } else if (projectId != null) {
            recordService.deleteByProjectId(projectId);
        } else {
            recordService.deleteAll();
        }
        return ResponseEntity.noContent().build();
    }
}
