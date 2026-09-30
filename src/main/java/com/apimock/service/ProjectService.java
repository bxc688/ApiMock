package com.apimock.service;

import com.apimock.mapper.MockApiMapper;
import com.apimock.mapper.ProjectMapper;
import com.apimock.model.entity.Project;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectMapper projectMapper;
    private final MockApiMapper mockApiMapper;

    public ProjectService(ProjectMapper projectMapper, MockApiMapper mockApiMapper) {
        this.projectMapper = projectMapper;
        this.mockApiMapper = mockApiMapper;
    }

    public List<Project> findAll() {
        return projectMapper.findAll();
    }

    public Project findById(Long id) {
        return projectMapper.findById(id);
    }

    public Project create(Project project) {
        projectMapper.insert(project);
        return project;
    }

    public Project update(Long id, Project project) {
        Project existing = projectMapper.findById(id);
        if (existing == null) {
            throw new RuntimeException("Project not found: " + id);
        }
        project.setId(id);
        projectMapper.update(project);
        return project;
    }

    public void delete(Long id) {
        if (id == 1) {
            throw new RuntimeException("不能删除默认项目");
        }
        int apiCount = mockApiMapper.countByProjectId(id);
        if (apiCount > 0) {
            throw new RuntimeException("该项目下存在 " + apiCount + " 个接口，不允许删除");
        }
        projectMapper.deleteById(id);
    }
}
