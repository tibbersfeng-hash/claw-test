package com.openclaw.test.dto;

import com.openclaw.test.entity.DesignDoc;
import java.time.LocalDateTime;

public class DesignDocResponse {

    private Long id;
    private String title;
    private String content;
    private String creator;
    private Long taskId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DesignDocResponse fromEntity(DesignDoc doc) {
        DesignDocResponse response = new DesignDocResponse();
        response.setId(doc.getId());
        response.setTitle(doc.getTitle());
        response.setContent(doc.getContent());
        response.setCreator(doc.getCreator());
        response.setTaskId(doc.getTaskId());
        response.setCreatedAt(doc.getCreatedAt());
        response.setUpdatedAt(doc.getUpdatedAt());
        return response;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
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
