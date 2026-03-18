package com.openclaw.test.dto;

import com.openclaw.test.entity.RequirementHistory;

import java.time.LocalDateTime;

public class RequirementHistoryResponse {

    private Long id;
    private Long requirementId;
    private Integer version;
    private String title;
    private String content;
    private String operationType;
    private String operator;
    private String remark;
    private LocalDateTime createdAt;

    public static RequirementHistoryResponse fromEntity(RequirementHistory history) {
        RequirementHistoryResponse response = new RequirementHistoryResponse();
        response.setId(history.getId());
        response.setRequirementId(history.getRequirementId());
        response.setVersion(history.getVersion());
        response.setTitle(history.getTitle());
        response.setContent(history.getContent());
        response.setOperationType(history.getOperationType());
        response.setOperator(history.getOperator());
        response.setRemark(history.getRemark());
        response.setCreatedAt(history.getCreatedAt());
        return response;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRequirementId() {
        return requirementId;
    }

    public void setRequirementId(Long requirementId) {
        this.requirementId = requirementId;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
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

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
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
}
