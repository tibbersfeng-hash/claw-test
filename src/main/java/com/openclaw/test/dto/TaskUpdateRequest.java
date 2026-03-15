package com.openclaw.test.dto;

import jakarta.validation.constraints.Size;

public class TaskUpdateRequest {

    @Size(max = 1000, message = "任务内容最大1000字符")
    private String content;

    private Long projectId;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}
