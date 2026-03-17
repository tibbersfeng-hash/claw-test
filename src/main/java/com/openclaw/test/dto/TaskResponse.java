package com.openclaw.test.dto;

import com.openclaw.test.entity.Task;
import com.openclaw.test.entity.TaskStatus;
import com.openclaw.test.entity.TaskType;

import java.time.LocalDateTime;

public class TaskResponse {

    private Long id;
    private String content;
    private String creator;
    private String handler;
    private Long projectId;
    private String projectName;
    private TaskStatus status;
    private TaskType taskType;
    private String taskTypeDisplay;
    private Long parentId;
    private String assigneeRole;
    private Integer priority;
    private String tags;
    private String imageUrls;
    private String extraData;
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
        response.setTaskType(task.getTaskType());
        if (task.getTaskType() != null) {
            response.setTaskTypeDisplay(task.getTaskType().getDisplayName());
        }
        response.setParentId(task.getParentId());
        response.setAssigneeRole(task.getAssigneeRole());
        response.setPriority(task.getPriority());
        response.setTags(task.getTags());
        response.setImageUrls(task.getImageUrls());
        response.setExtraData(task.getExtraData());
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

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public String getTaskTypeDisplay() {
        return taskTypeDisplay;
    }

    public void setTaskTypeDisplay(String taskTypeDisplay) {
        this.taskTypeDisplay = taskTypeDisplay;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getAssigneeRole() {
        return assigneeRole;
    }

    public void setAssigneeRole(String assigneeRole) {
        this.assigneeRole = assigneeRole;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(String imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
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
