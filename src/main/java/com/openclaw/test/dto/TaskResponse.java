package com.openclaw.test.dto;

import com.openclaw.test.entity.Task;
import com.openclaw.test.entity.TaskStatus;

import java.time.LocalDateTime;

public class TaskResponse {

    private Long id;
    private String content;
    private String creator;
    private String handler;
    private Long projectId;
    private String projectName;
    private TaskStatus status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TaskResponse fromEntity(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setContent(task.getContent());
        response.setCreator(task.getCreator());
        response.setHandler(task.getHandler());
        response.setProjectId(task.getProjectId());
        response.setStatus(task.getStatus());
        response.setRemark(task.getRemark());
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
