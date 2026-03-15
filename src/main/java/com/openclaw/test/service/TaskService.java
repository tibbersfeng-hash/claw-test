package com.openclaw.test.service;

import com.openclaw.test.dto.TaskCompleteRequest;
import com.openclaw.test.dto.TaskCreateRequest;
import com.openclaw.test.dto.TaskResponse;
import com.openclaw.test.dto.TaskUpdateRequest;
import com.openclaw.test.entity.Identity;
import com.openclaw.test.entity.IdentityType;
import com.openclaw.test.entity.Task;
import com.openclaw.test.entity.TaskStatus;
import com.openclaw.test.exception.TaskNotFoundException;
import com.openclaw.test.repository.IdentityRepository;
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

    public TaskService(TaskRepository taskRepository, IdentityRepository identityRepository) {
        this.taskRepository = taskRepository;
        this.identityRepository = identityRepository;
    }

    @Transactional
    public TaskResponse createTask(TaskCreateRequest request, Identity identity) {
        Task task = new Task();
        task.setContent(request.getContent());
        task.setCreator(getCreatorName(identity));
        task.setStatus(TaskStatus.INIT);

        // PM创建的任务，自动分配DEV作为处理人
        if (identity.getType() == IdentityType.PM) {
            identityRepository.findFirstByTypeOrderByIdAsc(IdentityType.DEV)
                    .ifPresent(dev -> task.setHandler(getCreatorName(dev)));
        }

        Task savedTask = taskRepository.save(task);
        return TaskResponse.fromEntity(savedTask);
    }

    private String getCreatorName(Identity identity) {
        return identity.getType().name() + "-" + identity.getId();
    }

    public Page<TaskResponse> getTasks(int page, int size, TaskStatus status, IdentityType identityType) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Task> taskPage;
        boolean hasStatus = status != null;
        boolean hasIdentityType = identityType != null;

        if (hasStatus && hasIdentityType) {
            taskPage = taskRepository.findByStatusAndCreatorStartingWith(status, identityType.name(), pageable);
        } else if (hasStatus) {
            taskPage = taskRepository.findByStatus(status, pageable);
        } else if (hasIdentityType) {
            taskPage = taskRepository.findByCreatorStartingWith(identityType.name(), pageable);
        } else {
            taskPage = taskRepository.findAll(pageable);
        }

        return taskPage.map(TaskResponse::fromEntity);
    }

    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        return TaskResponse.fromEntity(task);
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
        return TaskResponse.fromEntity(savedTask);
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
        return TaskResponse.fromEntity(savedTask);
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskUpdateRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        if (request.getContent() != null && !request.getContent().trim().isEmpty()) {
            task.setContent(request.getContent());
        }

        Task savedTask = taskRepository.save(task);
        return TaskResponse.fromEntity(savedTask);
    }

    @Transactional
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        taskRepository.deleteById(id);
    }
}
