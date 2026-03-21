package com.openclaw.test.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public class AgentMessageRequest {

    @NotBlank(message = "消息类型不能为空")
    private String type;

    private Map<String, Object> payload;

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
}
