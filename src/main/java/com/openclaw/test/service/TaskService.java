package com.openclaw.test.service;

import com.openclaw.test.dto.TaskCompleteRequest;
import com.openclaw.test.dto.TaskCreateRequest;
import com.openclaw.test.dto.TaskResponse;
import com.openclaw.test.dto.TaskUpdateRequest;
import com.openclaw.test.entity.*;
import com.openclaw.test.exception.TaskNotFoundException;
import com.openclaw.test.repository.IdentityRepository;
import com.openclaw.test.repository.ProjectRepository;
import com.openclaw.test.repository.RequirementRepository;
import com.openclaw.test.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final IdentityRepository identityRepository;
    private final ProjectRepository projectRepository;
    private final RequirementRepository requirementRepository;
    private final DesignDocService designDocService;

    public TaskService(TaskRepository taskRepository, IdentityRepository identityRepository, ProjectRepository projectRepository, RequirementRepository requirementRepository, DesignDocService designDocService) {
        this.taskRepository = taskRepository;
        this.identityRepository = identityRepository;
        this.projectRepository = projectRepository;
        this.requirementRepository = requirementRepository;
        this.designDocService = designDocService;
    }

    @Transactional
    public TaskResponse createTask(TaskCreateRequest request, Identity identity) {
        Task task = new Task();
        task.setContent(request.getContent());
        task.setCreator(getCreatorName(identity));
        task.setProjectId(request.getProjectId());

        // 设置任务类型
        TaskType taskType = request.getTaskType() != null ? request.getTaskType() : TaskType.REQ;
        task.setTaskType(taskType);

        // 设置父任务
        if (request.getParentId() != null) {
            task.setParentId(request.getParentId());
        }

        // 设置关联需求文档
        if (request.getRequirementId() != null) {
            task.setRequirementId(request.getRequirementId());
        }

        // 设置执行者角色
        if (request.getAssigneeRole() != null) {
            task.setAssigneeRole(request.getAssigneeRole());
        } else {
            task.setAssigneeRole(taskType.getDefaultHandlerRole());
        }

        // 设置优先级
        task.setPriority(request.getPriority() != null ? request.getPriority() : 0);

        // 设置标签
        if (request.getTags() != null) {
            task.setTags(request.getTags());
        }

        // 设置图片链接
        if (request.getImageUrls() != null) {
            task.setImageUrls(request.getImageUrls());
        }

        // 设置额外数据
        if (request.getExtraData() != null) {
            task.setExtraData(request.getExtraData());
        }

        // 自动分配处理人
        assignHandler(task, identity);

        Task savedTask = taskRepository.save(task);
        log.info("任务创建成功: id={}, type={}, creator={}", savedTask.getId(), taskType, identity.getType());

        return fillProjectName(TaskResponse.fromEntity(savedTask));
    }

    private void assignHandler(Task task, Identity creator) {
        String assigneeRole = task.getAssigneeRole();
        if (assigneeRole == null) {
            return;
        }

        try {
            IdentityType type = IdentityType.valueOf(assigneeRole);
            identityRepository.findFirstByTypeOrderByIdAsc(type)
                    .ifPresent(handler -> task.setHandler(getCreatorName(handler)));
        } catch (IllegalArgumentException e) {
            log.warn("无效的执行者角色: {}", assigneeRole);
        }
    }

    private String getCreatorName(Identity identity) {
        return identity.getType().name() + "-" + identity.getId();
    }

    private TaskResponse fillProjectName(TaskResponse response) {
        if (response.getProjectId() != null) {
            projectRepository.findById(response.getProjectId())
                    .ifPresent(project -> response.setProjectName(project.getName()));
        }
        return fillRequirementTitle(response);
    }

    private TaskResponse fillRequirementTitle(TaskResponse response) {
        if (response.getRequirementId() != null) {
            requirementRepository.findById(response.getRequirementId())
                    .ifPresent(requirement -> response.setRequirementTitle(requirement.getTitle()));
        }
        return response;
    }

    public Page<TaskResponse> getTasks(int page, int size, TaskStatus status, TaskType taskType, String assigneeRole, String creatorType) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Task> taskPage;

        // 按 creatorType 筛选（creator 字段格式为 "PM-1"、"DEV-2" 等）
        boolean hasCreatorType = creatorType != null && !creatorType.isEmpty();

        if (status != null && taskType != null && assigneeRole != null && hasCreatorType) {
            taskPage = taskRepository.findByStatusAndTaskTypeAndAssigneeRoleAndCreatorStartingWith(status, taskType, assigneeRole, creatorType, pageable);
        } else if (status != null && taskType != null && hasCreatorType) {
            taskPage = taskRepository.findByStatusAndTaskTypeAndCreatorStartingWith(status, taskType, creatorType, pageable);
        } else if (status != null && assigneeRole != null && hasCreatorType) {
            taskPage = taskRepository.findByStatusAndAssigneeRoleAndCreatorStartingWith(status, assigneeRole, creatorType, pageable);
        } else if (taskType != null && assigneeRole != null && hasCreatorType) {
            taskPage = taskRepository.findByTaskTypeAndAssigneeRoleAndCreatorStartingWith(taskType, assigneeRole, creatorType, pageable);
        } else if (status != null && hasCreatorType) {
            taskPage = taskRepository.findByStatusAndCreatorStartingWith(status, creatorType, pageable);
        } else if (taskType != null && hasCreatorType) {
            taskPage = taskRepository.findByTaskTypeAndCreatorStartingWith(taskType, creatorType, pageable);
        } else if (assigneeRole != null && hasCreatorType) {
            taskPage = taskRepository.findByAssigneeRoleAndCreatorStartingWith(assigneeRole, creatorType, pageable);
        } else if (hasCreatorType) {
            taskPage = taskRepository.findByCreatorStartingWith(creatorType, pageable);
        } else if (status != null && taskType != null && assigneeRole != null) {
            taskPage = taskRepository.findByStatusAndTaskTypeAndAssigneeRole(status, taskType, assigneeRole, pageable);
        } else if (status != null && taskType != null) {
            taskPage = taskRepository.findByStatusAndTaskType(status, taskType, pageable);
        } else if (status != null && assigneeRole != null) {
            taskPage = taskRepository.findByStatusAndAssigneeRole(status, assigneeRole, pageable);
        } else if (taskType != null && assigneeRole != null) {
            taskPage = taskRepository.findByTaskTypeAndAssigneeRole(taskType, assigneeRole, pageable);
        } else if (status != null) {
            taskPage = taskRepository.findByStatus(status, pageable);
        } else if (taskType != null) {
            taskPage = taskRepository.findByTaskType(taskType, pageable);
        } else if (assigneeRole != null) {
            taskPage = taskRepository.findByAssigneeRole(assigneeRole, pageable);
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

    /**
     * Agent 拉取待办任务
     */
    @Transactional
    public Optional<TaskResponse> pullTodoTask(String role) {
        Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "priority")
                .and(Sort.by(Sort.Direction.ASC, "createdAt")));

        Page<Task> taskPage = taskRepository.findByStatusAndAssigneeRole(
                TaskStatus.PENDING, role, pageable);

        if (taskPage.hasContent()) {
            Task task = taskPage.getContent().get(0);
            task.setStatus(TaskStatus.IN_PROGRESS);
            Task savedTask = taskRepository.save(task);
            log.info("Agent拉取任务: id={}, type={}, role={}", savedTask.getId(), savedTask.getTaskType(), role);
            return Optional.of(fillProjectName(TaskResponse.fromEntity(savedTask)));
        }

        return Optional.empty();
    }

    /**
     * 完成任务并自动创建下一阶段任务
     */
    @Transactional
    public TaskResponse completeTask(Long id, TaskCompleteRequest request, Identity identity) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        if (task.getStatus() == TaskStatus.COMPLETED || task.getStatus() == TaskStatus.CLOSED) {
            throw new IllegalStateException("任务已完成或已关闭，无法重复完成");
        }

        // 更新任务状态
        task.setStatus(TaskStatus.COMPLETED);

        if (request.getRemark() != null) {
            task.setRemark(request.getRemark());
        }

        if (request.getImageUrls() != null) {
            task.setImageUrls(request.getImageUrls());
        }

        if (request.getExtraData() != null) {
            task.setExtraData(request.getExtraData());
        }

        Task savedTask = taskRepository.save(task);
        log.info("任务完成: id={}, type={}", savedTask.getId(), savedTask.getTaskType());

        // 自动创建下一阶段任务
        if (Boolean.TRUE.equals(request.getCreateNextTask())) {
            createNextStageTask(savedTask, identity);
        }

        return fillProjectName(TaskResponse.fromEntity(savedTask));
    }

    /**
     * 创建下一阶段任务
     */
    private void createNextStageTask(Task completedTask, Identity identity) {
        TaskType nextType = completedTask.getTaskType().getNextTaskTypeEnum();

        if (nextType == null) {
            log.info("任务流程结束: id={}, type={}", completedTask.getId(), completedTask.getTaskType());
            return;
        }

        Task nextTask = new Task();
        nextTask.setContent("[" + nextType.getDisplayName() + "] " + extractOriginalContent(completedTask.getContent()));
        nextTask.setCreator(getCreatorName(identity));
        nextTask.setProjectId(completedTask.getProjectId());
        nextTask.setTaskType(nextType);
        nextTask.setParentId(completedTask.getId());
        nextTask.setAssigneeRole(nextType.getDefaultHandlerRole());
        nextTask.setPriority(completedTask.getPriority());

        // 继承父任务的标签
        if (completedTask.getTags() != null) {
            nextTask.setTags(completedTask.getTags());
        }

        // 自动分配处理人
        assignHandler(nextTask, identity);

        Task savedNextTask = taskRepository.save(nextTask);
        log.info("创建下一阶段任务: id={}, type={}, parentId={}", savedNextTask.getId(), nextType, completedTask.getId());
    }

    private String extractOriginalContent(String content) {
        // 移除任务类型前缀
        if (content == null) return "";
        return content.replaceAll("^\\[.+?\\]\\s*", "").replaceAll("^【.+?】\\s*", "");
    }

    /**
     * 拒绝任务（验收不通过）
     */
    @Transactional
    public TaskResponse rejectTask(Long id, String remark, Identity identity) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        task.setStatus(TaskStatus.REJECTED);

        String rejectRemark = "【拒绝】" + (remark != null ? remark : "");
        task.setRemark(rejectRemark);

        Task savedTask = taskRepository.save(task);
        log.info("任务拒绝: id={}, type={}, reason={}", savedTask.getId(), savedTask.getTaskType(), remark);

        // 创建返工任务
        createReworkTask(savedTask, identity);

        return fillProjectName(TaskResponse.fromEntity(savedTask));
    }

    /**
     * 创建返工任务
     */
    private void createReworkTask(Task rejectedTask, Identity identity) {
        TaskType taskType = rejectedTask.getTaskType();

        // 根据任务类型决定返工类型
        TaskType reworkType;
        if (taskType == TaskType.ACCEPT) {
            // 验收不通过，返回开发
            reworkType = TaskType.DEV;
        } else if (taskType == TaskType.TEST) {
            // 测试失败，创建Bug
            reworkType = TaskType.BUG;
        } else {
            log.info("任务类型不支持返工: {}", taskType);
            return;
        }

        Task reworkTask = new Task();
        reworkTask.setContent("【返工】" + extractOriginalContent(rejectedTask.getContent()));
        reworkTask.setCreator(getCreatorName(identity));
        reworkTask.setProjectId(rejectedTask.getProjectId());
        reworkTask.setTaskType(reworkType);
        reworkTask.setParentId(rejectedTask.getId());
        reworkTask.setAssigneeRole(reworkType.getDefaultHandlerRole());
        reworkTask.setPriority(rejectedTask.getPriority() + 1); // 提高优先级

        if (rejectedTask.getTags() != null) {
            reworkTask.setTags(rejectedTask.getTags());
        }

        assignHandler(reworkTask, identity);

        Task savedReworkTask = taskRepository.save(reworkTask);
        log.info("创建返工任务: id={}, type={}, parentId={}", savedReworkTask.getId(), reworkType, rejectedTask.getId());
    }

    @Transactional
    public TaskResponse startTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        if (task.getStatus() == TaskStatus.COMPLETED || task.getStatus() == TaskStatus.CLOSED) {
            throw new IllegalStateException("任务已完成或已关闭");
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

    /**
     * 获取任务链
     */
    public List<TaskResponse> getTaskChain(Long taskId) {
        return taskRepository.findByParentId(taskId).stream()
                .map(task -> fillProjectName(TaskResponse.fromEntity(task)))
                .collect(Collectors.toList());
    }
}
