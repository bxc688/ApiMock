package com.apimock.controller;

import com.apimock.model.dto.MockResponse;
import com.apimock.model.entity.Project;
import com.apimock.service.ProjectService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public MockResponse<List<Project>> list() {
        return MockResponse.ok(projectService.findAll());
    }

    @GetMapping("/{id}")
    public MockResponse<Project> getById(@PathVariable Long id) {
        Project project = projectService.findById(id);
        if (project == null) {
            return MockResponse.error(404, "Project not found");
        }
        return MockResponse.ok(project);
    }

    @PostMapping
    public MockResponse<Project> create(@RequestBody Project project) {
        try {
            Project created = projectService.create(project);
            return MockResponse.ok(created);
        } catch (Exception e) {
            return MockResponse.error(400, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public MockResponse<Project> update(@PathVariable Long id, @RequestBody Project project) {
        try {
            Project updated = projectService.update(id, project);
            return MockResponse.ok(updated);
        } catch (Exception e) {
            return MockResponse.error(400, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public MockResponse<Void> delete(@PathVariable Long id) {
        try {
            projectService.delete(id);
            return MockResponse.ok(null);
        } catch (Exception e) {
            return MockResponse.error(400, e.getMessage());
        }
    }
}
