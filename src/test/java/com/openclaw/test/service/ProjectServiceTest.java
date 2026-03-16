package com.openclaw.test.service;

import com.openclaw.test.dto.ProjectRequest;
import com.openclaw.test.dto.ProjectResponse;
import com.openclaw.test.entity.Project;
import com.openclaw.test.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectService projectService;

    private Project testProject;

    @BeforeEach
    void setUp() {
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("claw-test");
        testProject.setRepoUrl("https://github.com/test/repo.git");
        testProject.setProjectPath("/opt/codegspace/claw-test");
    }

    @Test
    @DisplayName("创建项目 - 成功")
    void createProject_Success() {
        // Arrange
        ProjectRequest request = new ProjectRequest();
        request.setName("new-project");
        request.setRepoUrl("https://github.com/test/new.git");
        request.setProjectPath("/opt/codegspace/new-project");

        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            project.setId(2L);
            return project;
        });

        // Act
        ProjectResponse response = projectService.createProject(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("new-project");
        assertThat(response.getRepoUrl()).isEqualTo("https://github.com/test/new.git");
        assertThat(response.getProjectPath()).isEqualTo("/opt/codegspace/new-project");
    }

    @Test
    @DisplayName("创建项目 - 仅必填字段")
    void createProject_RequiredFieldsOnly() {
        // Arrange
        ProjectRequest request = new ProjectRequest();
        request.setName("minimal-project");

        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            project.setId(3L);
            return project;
        });

        // Act
        ProjectResponse response = projectService.createProject(request);

        // Assert
        assertThat(response.getName()).isEqualTo("minimal-project");
        assertThat(response.getRepoUrl()).isNull();
        assertThat(response.getProjectPath()).isNull();
    }

    @Test
    @DisplayName("查询项目列表 - 无筛选条件")
    void getProjects_NoFilter() {
        // Arrange
        Page<Project> projectPage = new PageImpl<>(List.of(testProject));
        when(projectRepository.findAll(any(Pageable.class))).thenReturn(projectPage);

        // Act
        Page<ProjectResponse> response = projectService.getProjects(null, 0, 10);

        // Assert
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getName()).isEqualTo("claw-test");
    }

    @Test
    @DisplayName("查询项目列表 - 按名称筛选")
    void getProjects_WithNameFilter() {
        // Arrange
        Page<Project> projectPage = new PageImpl<>(List.of(testProject));
        when(projectRepository.findByNameContaining(eq("claw"), any(Pageable.class))).thenReturn(projectPage);

        // Act
        Page<ProjectResponse> response = projectService.getProjects("claw", 0, 10);

        // Assert
        assertThat(response.getContent()).hasSize(1);
        verify(projectRepository, times(1)).findByNameContaining(eq("claw"), any(Pageable.class));
    }

    @Test
    @DisplayName("查询项目列表 - 分页")
    void getProjects_Pagination() {
        // Arrange
        Project project2 = new Project();
        project2.setId(2L);
        project2.setName("project-2");

        Page<Project> projectPage = new PageImpl<>(List.of(testProject, project2));
        when(projectRepository.findAll(any(Pageable.class))).thenReturn(projectPage);

        // Act
        Page<ProjectResponse> response = projectService.getProjects(null, 0, 10);

        // Assert
        assertThat(response.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("根据ID查询项目 - 成功")
    void getProject_Success() {
        // Arrange
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

        // Act
        ProjectResponse response = projectService.getProject(1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("claw-test");
    }

    @Test
    @DisplayName("根据ID查询项目 - 不存在")
    void getProject_NotFound() {
        // Arrange
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> projectService.getProject(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("项目不存在");
    }

    @Test
    @DisplayName("更新项目 - 成功")
    void updateProject_Success() {
        // Arrange
        ProjectRequest request = new ProjectRequest();
        request.setName("updated-project");
        request.setRepoUrl("https://github.com/updated/repo.git");
        request.setProjectPath("/opt/updated");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);

        // Act
        ProjectResponse response = projectService.updateProject(1L, request);

        // Assert
        assertThat(response).isNotNull();
        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    @DisplayName("更新项目 - 部分字段")
    void updateProject_PartialUpdate() {
        // Arrange
        ProjectRequest request = new ProjectRequest();
        request.setName("updated-name");
        request.setRepoUrl(null);
        request.setProjectPath(null);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);

        // Act
        ProjectResponse response = projectService.updateProject(1L, request);

        // Assert
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("更新项目 - 不存在")
    void updateProject_NotFound() {
        // Arrange
        ProjectRequest request = new ProjectRequest();
        request.setName("updated");

        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> projectService.updateProject(999L, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("项目不存在");
    }

    @Test
    @DisplayName("删除项目 - 成功")
    void deleteProject_Success() {
        // Arrange
        when(projectRepository.existsById(1L)).thenReturn(true);

        // Act
        projectService.deleteProject(1L);

        // Assert
        verify(projectRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("删除项目 - 不存在")
    void deleteProject_NotFound() {
        // Arrange
        when(projectRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> projectService.deleteProject(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("项目不存在");
    }
}
