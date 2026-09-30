package com.apimock.mapper;

import com.apimock.model.entity.Project;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProjectMapper {

    @Select("SELECT * FROM project ORDER BY id ASC")
    List<Project> findAll();

    @Select("SELECT * FROM project WHERE id = #{id}")
    Project findById(@Param("id") Long id);

    @Insert("INSERT INTO project (name, description) VALUES (#{name}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Project project);

    @Update("UPDATE project SET name=#{name}, description=#{description}, updated_at=datetime('now','localtime') WHERE id=#{id}")
    int update(Project project);

    @Delete("DELETE FROM project WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}
