package com.openclaw.test.dto;

import com.openclaw.test.entity.Requirement;
import com.openclaw.test.entity.RequirementStatus;

import java.time.LocalDateTime;

public class RequirementResponse {

    private Long id;
    private String requirementNumber;
    private String title;
    private String content;
    private String creator;
    private RequirementStatus status;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RequirementResponse fromEntity(Requirement requirement) {
        RequirementResponse response = new RequirementResponse();
        response.setId(requirement.getId());
        response.setRequirementNumber(requirement.getRequirementNumber());
        response.setTitle(requirement.getTitle());
        response.setContent(requirement.getContent());
        response.setCreator(requirement.getCreator());
        response.setStatus(requirement.getStatus());
        response.setVersion(requirement.getVersion());
        response.setCreatedAt(requirement.getCreatedAt());
        response.setUpdatedAt(requirement.getUpdatedAt());
        return response;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRequirementNumber() {
        return requirementNumber;
    }

    public void setRequirementNumber(String requirementNumber) {
        this.requirementNumber = requirementNumber;
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

    public RequirementStatus getStatus() {
        return status;
    }

    public void setStatus(RequirementStatus status) {
        this.status = status;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
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
