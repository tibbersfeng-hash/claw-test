package com.openclaw.test.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "identities")
public class Identity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IdentityType type;

    @Column(nullable = false, unique = true, length = 64)
    private String apiKey;

    /**
     * 身份标识符，如 "PM-1", "DEV-3"
     * 用于 Agent 间通信的身份认证
     */
    @Column(unique = true, length = 32)
    private String identityId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = generateApiKey();
        }
        if (identityId == null || identityId.isEmpty()) {
            identityId = type.name() + "-" + id;
        }
    }

    private String generateApiKey() {
        return "sk-" + java.util.UUID.randomUUID().toString().replace("-", "");
    }

    // Getters and Setters
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

    public String getIdentityId() {
        return identityId;
    }

    public void setIdentityId(String identityId) {
        this.identityId = identityId;
    }
}
