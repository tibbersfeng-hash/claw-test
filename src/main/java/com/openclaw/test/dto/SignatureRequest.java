package com.openclaw.test.dto;

import jakarta.validation.constraints.NotNull;

public class SignatureRequest {

    @NotNull(message = "时间戳不能为空")
    private Long timestamp;

    private String body;

    // Getters and Setters
    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
