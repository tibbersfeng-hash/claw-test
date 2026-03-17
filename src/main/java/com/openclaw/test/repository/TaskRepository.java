package com.openclaw.test.repository;

import com.openclaw.test.entity.Task;
import com.openclaw.test.entity.TaskStatus;
import com.openclaw.test.entity.TaskType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    Page<Task> findByTaskType(TaskType taskType, Pageable pageable);

    Page<Task> findByAssigneeRole(String assigneeRole, Pageable pageable);

    Page<Task> findByStatusAndTaskType(TaskStatus status, TaskType taskType, Pageable pageable);

    Page<Task> findByStatusAndAssigneeRole(TaskStatus status, String assigneeRole, Pageable pageable);

    Page<Task> findByTaskTypeAndAssigneeRole(TaskType taskType, String assigneeRole, Pageable pageable);

    Page<Task> findByStatusAndTaskTypeAndAssigneeRole(TaskStatus status, TaskType taskType, String assigneeRole, Pageable pageable);

    List<Task> findByParentId(Long parentId);

    // 兼容旧接口
    Page<Task> findByCreatorStartingWith(String identityType, Pageable pageable);

    Page<Task> findByStatusAndCreatorStartingWith(TaskStatus status, String identityType, Pageable pageable);

    Page<Task> findByHandlerStartingWith(String identityType, Pageable pageable);

    Page<Task> findByStatusAndHandlerStartingWith(TaskStatus status, String identityType, Pageable pageable);

    Page<Task> findByCreatorStartingWithAndHandlerStartingWith(String creatorType, String handlerType, Pageable pageable);

    Page<Task> findByStatusAndCreatorStartingWithAndHandlerStartingWith(TaskStatus status, String creatorType, String handlerType, Pageable pageable);
}
