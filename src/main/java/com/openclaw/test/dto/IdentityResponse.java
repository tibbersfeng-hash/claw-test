package com.openclaw.test.dto;

import com.openclaw.test.entity.Identity;
import com.openclaw.test.entity.IdentityType;

import java.time.LocalDateTime;

public class IdentityResponse {

    private Long id;
    private IdentityType type;
    private String apiKey;
    private LocalDateTime createdAt;

    public static IdentityResponse fromEntity(Identity identity) {
        IdentityResponse response = new IdentityResponse();
        response.setId(identity.getId());
        response.setType(identity.getType());
        response.setApiKey(identity.getApiKey());
        response.setCreatedAt(identity.getCreatedAt());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public IdentityType getType() {
        return type;
    }

    public void setType(IdentityType type) {
        this.type = type;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
