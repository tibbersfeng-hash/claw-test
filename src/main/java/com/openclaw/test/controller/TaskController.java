package com.openclaw.test.controller;

import com.openclaw.test.config.AuthInterceptor;
import com.openclaw.test.dto.TaskCompleteRequest;
import com.openclaw.test.dto.TaskCreateRequest;
import com.openclaw.test.dto.TaskResponse;
import com.openclaw.test.dto.TaskUpdateRequest;
import com.openclaw.test.entity.Identity;
import com.openclaw.test.entity.IdentityType;
import com.openclaw.test.entity.TaskStatus;
import com.openclaw.test.entity.TaskType;
import com.openclaw.test.service.TaskService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody TaskCreateRequest request,
            HttpServletRequest httpRequest) {
        Identity identity = (Identity) httpRequest.getAttribute(AuthInterceptor.IDENTITY_ATTRIBUTE);
        TaskResponse response = taskService.createTask(request, identity);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<TaskResponse>> getTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskType taskType,
            @RequestParam(required = false) String assigneeRole) {

        size = Math.min(size, 100);
        page = Math.max(page, 0);

        Page<TaskResponse> response = taskService.getTasks(page, size, status, taskType, assigneeRole);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        TaskResponse response = taskService.getTaskById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Agent 拉取待办任务
     * GET /api/tasks/todo?role=DEV
     */
    @GetMapping("/todo")
    public ResponseEntity<TaskResponse> pullTodoTask(@RequestParam String role) {
        Optional<TaskResponse> response = taskService.pullTodoTask(role);
        return response
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    /**
     * 获取任务链（子任务列表）
     */
    @GetMapping("/{id}/chain")
    public ResponseEntity<List<TaskResponse>> getTaskChain(@PathVariable Long id) {
        List<TaskResponse> response = taskService.getTaskChain(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/start")
    public ResponseEntity<TaskResponse> startTask(@PathVariable Long id) {
        TaskResponse response = taskService.startTask(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<TaskResponse> completeTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskCompleteRequest request,
            HttpServletRequest httpRequest) {
        Identity identity = (Identity) httpRequest.getAttribute(AuthInterceptor.IDENTITY_ATTRIBUTE);
        TaskResponse response = taskService.completeTask(id, request, identity);
        return ResponseEntity.ok(response);
    }

    /**
     * 拒绝任务（验收不通过）
     */
    @PutMapping("/{id}/reject")
    public ResponseEntity<TaskResponse> rejectTask(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpServletRequest httpRequest) {
        Identity identity = (Identity) httpRequest.getAttribute(AuthInterceptor.IDENTITY_ATTRIBUTE);
        String remark = body.get("remark");
        TaskResponse response = taskService.rejectTask(id, remark, identity);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskUpdateRequest request) {
        TaskResponse response = taskService.updateTask(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
