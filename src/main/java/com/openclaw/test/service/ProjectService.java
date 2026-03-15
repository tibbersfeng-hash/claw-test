package com.openclaw.test.service;

import com.openclaw.test.dto.ProjectRequest;
import com.openclaw.test.dto.ProjectResponse;
import com.openclaw.test.entity.Project;
import com.openclaw.test.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {
        Project project = new Project();
        project.setName(request.getName());
        project.setRepoUrl(request.getRepoUrl());
        project.setProjectPath(request.getProjectPath());

        Project savedProject = projectRepository.save(project);
        return ProjectResponse.fromEntity(savedProject);
    }

    public Page<ProjectResponse> getProjects(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));

        Page<Project> projects;
        if (name != null && !name.trim().isEmpty()) {
            projects = projectRepository.findByNameContaining(name, pageable);
        } else {
            projects = projectRepository.findAll(pageable);
        }

        return projects.map(ProjectResponse::fromEntity);
    }

    public ProjectResponse getProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在: " + id));
        return ProjectResponse.fromEntity(project);
    }

    @Transactional
    public ProjectResponse updateProject(Long id, ProjectRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在: " + id));

        project.setName(request.getName());
        project.setRepoUrl(request.getRepoUrl());
        project.setProjectPath(request.getProjectPath());

        Project savedProject = projectRepository.save(project);
        return ProjectResponse.fromEntity(savedProject);
    }

    @Transactional
    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new RuntimeException("项目不存在: " + id);
        }
        projectRepository.deleteById(id);
    }
}
