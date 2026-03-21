package com.openclaw.test.dto;

import java.util.Map;

public class AgentMessageResponse {

    private boolean success;
    private String message;
    private Map<String, Object> data;

    public AgentMessageResponse() {}

    public AgentMessageResponse(boolean success, String message, Map<String, Object> data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
