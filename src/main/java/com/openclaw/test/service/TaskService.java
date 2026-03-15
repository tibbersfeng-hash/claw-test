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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final IdentityRepository identityRepository;
    private final ProjectRepository projectRepository;

    public TaskService(TaskRepository taskRepository, IdentityRepository identityRepository, ProjectRepository projectRepository) {
        this.taskRepository = taskRepository;
        this.identityRepository = identityRepository;
        this.projectRepository = projectRepository;
    }

    @Transactional
    public TaskResponse createTask(TaskCreateRequest request, Identity identity) {
        Task task = new Task();
        task.setContent(request.getContent());
        task.setCreator(getCreatorName(identity));
        task.setProjectId(request.getProjectId());
        task.setStatus(TaskStatus.INIT);

        // PM创建的任务，自动分配DEV作为处理人
        if (identity.getType() == IdentityType.PM) {
            identityRepository.findFirstByTypeOrderByIdAsc(IdentityType.DEV)
                    .ifPresent(dev -> task.setHandler(getCreatorName(dev)));
        }

        Task savedTask = taskRepository.save(task);
        return fillProjectName(TaskResponse.fromEntity(savedTask));
    }

    private String getCreatorName(Identity identity) {
        return identity.getType().name() + "-" + identity.getId();
    }

    private TaskResponse fillProjectName(TaskResponse response) {
        if (response.getProjectId() != null) {
            projectRepository.findById(response.getProjectId())
                    .ifPresent(project -> response.setProjectName(project.getName()));
        }
        return response;
    }

    public Page<TaskResponse> getTasks(int page, int size, TaskStatus status, IdentityType creatorType, IdentityType handlerType) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Task> taskPage;
        boolean hasStatus = status != null;
        boolean hasCreatorType = creatorType != null;
        boolean hasHandlerType = handlerType != null;

        if (hasStatus && hasCreatorType && hasHandlerType) {
            taskPage = taskRepository.findByStatusAndCreatorStartingWithAndHandlerStartingWith(status, creatorType.name(), handlerType.name(), pageable);
        } else if (hasStatus && hasCreatorType) {
            taskPage = taskRepository.findByStatusAndCreatorStartingWith(status, creatorType.name(), pageable);
        } else if (hasStatus && hasHandlerType) {
            taskPage = taskRepository.findByStatusAndHandlerStartingWith(status, handlerType.name(), pageable);
        } else if (hasCreatorType && hasHandlerType) {
            taskPage = taskRepository.findByCreatorStartingWithAndHandlerStartingWith(creatorType.name(), handlerType.name(), pageable);
        } else if (hasStatus) {
            taskPage = taskRepository.findByStatus(status, pageable);
        } else if (hasCreatorType) {
            taskPage = taskRepository.findByCreatorStartingWith(creatorType.name(), pageable);
        } else if (hasHandlerType) {
            taskPage = taskRepository.findByHandlerStartingWith(handlerType.name(), pageable);
        } else {
            taskPage = taskRepository.findAll(pageable);
        }

        return taskPage.map(task -> fillProjectName(TaskResponse.fromEntity(task)));
    }

    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        return fillProjectName(TaskResponse.fromEntity(task));
    }

    @Transactional
    public TaskResponse completeTask(Long id, TaskCompleteRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw new IllegalStateException("任务已完成，无法重复完成");
        }

        task.setStatus(TaskStatus.COMPLETED);
        task.setRemark(request.getRemark());

        Task savedTask = taskRepository.save(task);
        return fillProjectName(TaskResponse.fromEntity(savedTask));
    }

    @Transactional
    public TaskResponse startTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw new IllegalStateException("任务已完成，无法标记进行中");
        }

        task.setStatus(TaskStatus.IN_PROGRESS);

        Task savedTask = taskRepository.save(task);
        return fillProjectName(TaskResponse.fromEntity(savedTask));
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskUpdateRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        if (request.getContent() != null && !request.getContent().trim().isEmpty()) {
            task.setContent(request.getContent());
        }

        if (request.getProjectId() != null) {
            task.setProjectId(request.getProjectId());
        }

        Task savedTask = taskRepository.save(task);
        return fillProjectName(TaskResponse.fromEntity(savedTask));
    }

    @Transactional
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        taskRepository.deleteById(id);
    }
}
