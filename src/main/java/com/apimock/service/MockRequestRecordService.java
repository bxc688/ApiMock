package com.apimock.service;

import com.apimock.mapper.MockRequestRecordMapper;
import com.apimock.model.entity.MockRequestRecord;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MockRequestRecordService {

    private final MockRequestRecordMapper recordMapper;

    public MockRequestRecordService(MockRequestRecordMapper recordMapper) {
        this.recordMapper = recordMapper;
    }

    public List<MockRequestRecord> findAll(int limit, int offset) {
        return recordMapper.findAll(limit, offset);
    }

    public int countAll() {
        return recordMapper.countAll();
    }

    public List<MockRequestRecord> findByProjectId(Long projectId, int limit, int offset) {
        return recordMapper.findByProjectId(projectId, limit, offset);
    }

    public int countByProjectId(Long projectId) {
        return recordMapper.countByProjectId(projectId);
    }

    public List<MockRequestRecord> findByMockApiId(Long mockApiId, int limit, int offset) {
        return recordMapper.findByMockApiId(mockApiId, limit, offset);
    }

    public MockRequestRecord findById(Long id) {
        return recordMapper.findById(id);
    }

    public void save(MockRequestRecord record) {
        recordMapper.insert(record);
    }

    public void deleteByMockApiId(Long mockApiId) {
        recordMapper.deleteByMockApiId(mockApiId);
    }

    public void deleteByProjectId(Long projectId) {
        recordMapper.deleteByProjectId(projectId);
    }

    public void deleteAll() {
        recordMapper.deleteAll();
    }

    public List<MockRequestRecord> searchByPath(String searchPath, int limit, int offset) {
        return recordMapper.searchByPath(searchPath, limit, offset);
    }

    public int countByPath(String searchPath) {
        return recordMapper.countByPath(searchPath);
    }

    public List<MockRequestRecord> searchByApiName(String searchName, int limit, int offset) {
        return recordMapper.searchByApiName(searchName, limit, offset);
    }

    public int countByApiName(String searchName) {
        return recordMapper.countByApiName(searchName);
    }
}
