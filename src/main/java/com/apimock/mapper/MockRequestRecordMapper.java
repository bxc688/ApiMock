package com.apimock.mapper;

import com.apimock.model.entity.MockRequestRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MockRequestRecordMapper {

    @Select("SELECT * FROM mock_request_record ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<MockRequestRecord> findAll(@Param("limit") int limit, @Param("offset") int offset);

    @Select("SELECT COUNT(*) FROM mock_request_record")
    int countAll();

    @Select("SELECT * FROM mock_request_record WHERE project_id = #{projectId} ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<MockRequestRecord> findByProjectId(@Param("projectId") Long projectId, @Param("limit") int limit, @Param("offset") int offset);

    @Select("SELECT COUNT(*) FROM mock_request_record WHERE project_id = #{projectId}")
    int countByProjectId(@Param("projectId") Long projectId);

    @Select("SELECT * FROM mock_request_record WHERE mock_api_id = #{mockApiId} ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<MockRequestRecord> findByMockApiId(@Param("mockApiId") Long mockApiId, @Param("limit") int limit, @Param("offset") int offset);

    @Select("SELECT * FROM mock_request_record WHERE id = #{id}")
    MockRequestRecord findById(@Param("id") Long id);

    @Insert("INSERT INTO mock_request_record (project_id, mock_api_id, method, path, client_ip, request_headers, request_body, query_params, response_status, response_body, matched) " +
            "VALUES (#{projectId}, #{mockApiId}, #{method}, #{path}, #{clientIp}, #{requestHeaders}, #{requestBody}, #{queryParams}, #{responseStatus}, #{responseBody}, #{matched})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(MockRequestRecord record);

    @Delete("DELETE FROM mock_request_record WHERE mock_api_id = #{mockApiId}")
    int deleteByMockApiId(@Param("mockApiId") Long mockApiId);

    @Delete("DELETE FROM mock_request_record WHERE project_id = #{projectId}")
    int deleteByProjectId(@Param("projectId") Long projectId);

    @Delete("DELETE FROM mock_request_record")
    int deleteAll();

    @Select("SELECT * FROM mock_request_record WHERE path LIKE '%' || #{searchPath} || '%' ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<MockRequestRecord> searchByPath(@Param("searchPath") String searchPath, @Param("limit") int limit, @Param("offset") int offset);

    @Select("SELECT COUNT(*) FROM mock_request_record WHERE path LIKE '%' || #{searchPath} || '%'")
    int countByPath(@Param("searchPath") String searchPath);

    @Select("SELECT r.* FROM mock_request_record r LEFT JOIN mock_api a ON r.mock_api_id = a.id WHERE a.name LIKE '%' || #{searchName} || '%' ORDER BY r.created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<MockRequestRecord> searchByApiName(@Param("searchName") String searchName, @Param("limit") int limit, @Param("offset") int offset);

    @Select("SELECT COUNT(*) FROM mock_request_record r LEFT JOIN mock_api a ON r.mock_api_id = a.id WHERE a.name LIKE '%' || #{searchName} || '%'")
    int countByApiName(@Param("searchName") String searchName);
}
