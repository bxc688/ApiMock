package com.apimock.mapper;

import com.apimock.model.entity.MockApi;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MockApiMapper {

    @Select("SELECT * FROM mock_api ORDER BY created_at DESC")
    List<MockApi> findAll();

    @Select("SELECT * FROM mock_api WHERE project_id = #{projectId} ORDER BY created_at DESC")
    List<MockApi> findByProjectId(@Param("projectId") Long projectId);

    @Select("SELECT * FROM mock_api WHERE id = #{id}")
    MockApi findById(@Param("id") Long id);

    @Select("SELECT * FROM mock_api WHERE method = #{method} AND path = #{path} AND enabled = 1 LIMIT 1")
    MockApi findByMethodAndPath(@Param("method") String method, @Param("path") String path);

    @Insert("INSERT INTO mock_api (project_id, name, method, path, enabled, response_status, response_headers, response_body, " +
            "response_type, response_file_path, response_file_name, response_content_type, dynamic_rules, delay_ms, error_type, description) " +
            "VALUES (#{projectId}, #{name}, #{method}, #{path}, #{enabled}, #{responseStatus}, #{responseHeaders}, #{responseBody}, " +
            "#{responseType}, #{responseFilePath}, #{responseFileName}, #{responseContentType}, #{dynamicRules}, #{delayMs}, #{errorType}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(MockApi mockApi);

    @Update("UPDATE mock_api SET project_id=#{projectId}, name=#{name}, method=#{method}, path=#{path}, enabled=#{enabled}, " +
            "response_status=#{responseStatus}, response_headers=#{responseHeaders}, response_body=#{responseBody}, " +
            "response_type=#{responseType}, response_file_path=#{responseFilePath}, response_file_name=#{responseFileName}, response_content_type=#{responseContentType}, " +
            "dynamic_rules=#{dynamicRules}, " +
            "delay_ms=#{delayMs}, error_type=#{errorType}, description=#{description}, " +
            "updated_at=datetime('now','localtime') WHERE id=#{id}")
    int update(MockApi mockApi);

    @Update("UPDATE mock_api SET enabled = #{enabled}, updated_at = datetime('now','localtime') WHERE id = #{id}")
    int updateEnabled(@Param("id") Long id, @Param("enabled") Integer enabled);

    @Delete("DELETE FROM mock_api WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT * FROM mock_api WHERE method = #{method} AND path = #{path} LIMIT 1")
    MockApi findByMethodAndPathAny(@Param("method") String method, @Param("path") String path);

    @Select("SELECT COUNT(*) FROM mock_api WHERE project_id = #{projectId}")
    int countByProjectId(@Param("projectId") Long projectId);
}
