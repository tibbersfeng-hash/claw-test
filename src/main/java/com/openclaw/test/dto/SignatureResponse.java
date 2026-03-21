package com.openclaw.test.dto;

public class SignatureResponse {

    private String senderId;
    private Long timestamp;
    private String signature;

    public SignatureResponse() {}

    public SignatureResponse(String senderId, Long timestamp, String signature) {
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.signature = signature;
    }

    // Getters and Setters
    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
