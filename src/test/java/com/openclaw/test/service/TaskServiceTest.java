package com.openclaw.test.service;

import com.openclaw.test.dto.TaskCompleteRequest;
import com.openclaw.test.dto.TaskCreateRequest;
import com.openclaw.test.dto.TaskResponse;
import com.openclaw.test.dto.TaskUpdateRequest;
import com.openclaw.test.entity.Identity;
import com.openclaw.test.entity.IdentityType;
import com.openclaw.test.entity.Project;
import com.openclaw.test.entity.Task;
import com.openclaw.test.entity.TaskStatus;
import com.openclaw.test.exception.TaskNotFoundException;
import com.openclaw.test.repository.IdentityRepository;
import com.openclaw.test.repository.ProjectRepository;
import com.openclaw.test.repository.TaskRepository;
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
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private IdentityRepository identityRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private TaskService taskService;

    private Task testTask;
    private Identity pmIdentity;
    private Identity devIdentity;
    private Identity qaIdentity;
    private Project testProject;

    @BeforeEach
    void setUp() {
        pmIdentity = new Identity();
        pmIdentity.setId(1L);
        pmIdentity.setType(IdentityType.PM);

        devIdentity = new Identity();
        devIdentity.setId(2L);
        devIdentity.setType(IdentityType.DEV);

        qaIdentity = new Identity();
        qaIdentity.setId(3L);
        qaIdentity.setType(IdentityType.QA);

        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("claw-test");

        testTask = new Task();
        testTask.setId(1L);
        testTask.setContent("测试任务");
        testTask.setCreator("PM-1");
        testTask.setStatus(TaskStatus.INIT);
        testTask.setProjectId(1L);
    }

    @Test
    @DisplayName("创建任务 - PM创建时自动分配DEV")
    void createTask_PM_AssignDEV() {
        // Arrange
        TaskCreateRequest request = new TaskCreateRequest();
        request.setContent("新任务");
        request.setProjectId(1L);

        when(identityRepository.findFirstByTypeOrderByIdAsc(IdentityType.DEV))
                .thenReturn(Optional.of(devIdentity));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(1L);
            return task;
        });
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

        // Act
        TaskResponse response = taskService.createTask(request, pmIdentity);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getHandler()).isEqualTo("DEV-2");
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("创建任务 - DEV创建时自动分配QA")
    void createTask_DEV_AssignQA() {
        // Arrange
        TaskCreateRequest request = new TaskCreateRequest();
        request.setContent("新任务");

        when(identityRepository.findFirstByTypeOrderByIdAsc(IdentityType.QA))
                .thenReturn(Optional.of(qaIdentity));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(1L);
            return task;
        });

        // Act
        TaskResponse response = taskService.createTask(request, devIdentity);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getHandler()).isEqualTo("QA-3");
    }

    @Test
    @DisplayName("创建任务 - 关联项目")
    void createTask_WithProject() {
        // Arrange
        TaskCreateRequest request = new TaskCreateRequest();
        request.setContent("新任务");
        request.setProjectId(1L);

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(1L);
            return task;
        });
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

        // Act
        TaskResponse response = taskService.createTask(request, pmIdentity);

        // Assert
        assertThat(response.getProjectId()).isEqualTo(1L);
        assertThat(response.getProjectName()).isEqualTo("claw-test");
    }

    @Test
    @DisplayName("查询任务列表 - 无筛选条件")
    void getTasks_NoFilter() {
        // Arrange
        Page<Task> taskPage = new PageImpl<>(List.of(testTask));
        when(taskRepository.findAll(any(Pageable.class))).thenReturn(taskPage);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

        // Act
        Page<TaskResponse> response = taskService.getTasks(0, 10, null, null, null);

        // Assert
        assertThat(response.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("查询任务列表 - 按状态筛选")
    void getTasks_WithStatusFilter() {
        // Arrange
        Page<Task> taskPage = new PageImpl<>(List.of(testTask));
        when(taskRepository.findByStatus(eq(TaskStatus.INIT), any(Pageable.class))).thenReturn(taskPage);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

        // Act
        Page<TaskResponse> response = taskService.getTasks(0, 10, TaskStatus.INIT, null, null);

        // Assert
        assertThat(response.getContent()).hasSize(1);
        verify(taskRepository, times(1)).findByStatus(eq(TaskStatus.INIT), any(Pageable.class));
    }

    @Test
    @DisplayName("根据ID查询任务 - 成功")
    void getTaskById_Success() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

        // Act
        TaskResponse response = taskService.getTaskById(1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("根据ID查询任务 - 不存在")
    void getTaskById_NotFound() {
        // Arrange
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> taskService.getTaskById(999L))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    @DisplayName("开始任务 - 成功")
    void startTask_Success() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // Act
        TaskResponse response = taskService.startTask(1L);

        // Assert
        assertThat(response.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("开始任务 - 已完成不能开始")
    void startTask_AlreadyCompleted() {
        // Arrange
        testTask.setStatus(TaskStatus.COMPLETED);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        // Act & Assert
        assertThatThrownBy(() -> taskService.startTask(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("任务已完成");
    }

    @Test
    @DisplayName("完成任务 - 成功")
    void completeTask_Success() {
        // Arrange
        TaskCompleteRequest request = new TaskCompleteRequest();
        request.setRemark("完成备注");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

        // Act
        TaskResponse response = taskService.completeTask(1L, request);

        // Assert
        assertThat(response.getStatus()).isEqualTo(TaskStatus.COMPLETED);
    }

    @Test
    @DisplayName("完成任务 - 重复完成")
    void completeTask_AlreadyCompleted() {
        // Arrange
        testTask.setStatus(TaskStatus.COMPLETED);
        TaskCompleteRequest request = new TaskCompleteRequest();
        request.setRemark("完成备注");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        // Act & Assert
        assertThatThrownBy(() -> taskService.completeTask(1L, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("任务已完成");
    }

    @Test
    @DisplayName("更新任务 - 修改内容")
    void updateTask_ModifyContent() {
        // Arrange
        TaskUpdateRequest request = new TaskUpdateRequest();
        request.setContent("修改后的内容");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

        // Act
        TaskResponse response = taskService.updateTask(1L, request);

        // Assert
        assertThat(response).isNotNull();
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("更新任务 - 修改项目")
    void updateTask_ModifyProject() {
        // Arrange
        TaskUpdateRequest request = new TaskUpdateRequest();
        request.setProjectId(2L);

        Project newProject = new Project();
        newProject.setId(2L);
        newProject.setName("new-project");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        when(projectRepository.findById(2L)).thenReturn(Optional.of(newProject));

        // Act
        TaskResponse response = taskService.updateTask(1L, request);

        // Assert
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("删除任务 - 成功")
    void deleteTask_Success() {
        // Arrange
        when(taskRepository.existsById(1L)).thenReturn(true);

        // Act
        taskService.deleteTask(1L);

        // Assert
        verify(taskRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("删除任务 - 不存在")
    void deleteTask_NotFound() {
        // Arrange
        when(taskRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> taskService.deleteTask(999L))
                .isInstanceOf(TaskNotFoundException.class);
    }
}
