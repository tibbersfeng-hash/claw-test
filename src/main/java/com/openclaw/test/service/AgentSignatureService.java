package com.openclaw.test.service;

import com.openclaw.test.entity.Identity;
import com.openclaw.test.repository.IdentityRepository;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

/**
 * Agent 间通信签名服务
 * 提供身份验证和防伪造功能
 */
@Service
public class AgentSignatureService {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final String SHA256 = "SHA-256";
    private static final long TIMESTAMP_TOLERANCE_MS = 5 * 60 * 1000; // 5分钟容忍

    private final IdentityRepository identityRepository;

    public AgentSignatureService(IdentityRepository identityRepository) {
        this.identityRepository = identityRepository;
    }

    /**
     * 生成签名
     * @param apiKey 发送方的 API Key
     * @param senderId 发送方身份标识 (如 "PM-1")
     * @param timestamp 时间戳（毫秒）
     * @return 签名字符串
     */
    public String generateSignature(String apiKey, String senderId, long timestamp) {
        try {
            String message = senderId + "|" + timestamp;
            return hmacSha256(apiKey, message);
        } catch (Exception e) {
            throw new RuntimeException("签名生成失败", e);
        }
    }

    /**
     * 兼容旧方法
     */
    public String generateSignature(String apiKey, String senderId, long timestamp, String body) {
        return generateSignature(apiKey, senderId, timestamp);
    }

    /**
     * 验证签名
     * @param senderId 声称的发送方身份
     * @param timestamp 时间戳
     * @param signature 待验证的签名
     * @return 验证结果
     */
    public SignatureResult verifySignature(String senderId, long timestamp, String signature) {
        // 1. 检查时间戳
        long now = Instant.now().toEpochMilli();
        if (Math.abs(now - timestamp) > TIMESTAMP_TOLERANCE_MS) {
            return SignatureResult.failed("请求已过期");
        }

        // 2. 查找发送方身份
        Identity sender = identityRepository.findByIdentityId(senderId);
        if (sender == null) {
            return SignatureResult.failed("发送方身份不存在: " + senderId);
        }

        // 3. 计算期望签名
        String expectedSignature = generateSignature(sender.getApiKey(), senderId, timestamp);

        // 4. 常量时间比较（防止时序攻击）
        if (constantTimeEquals(expectedSignature, signature)) {
            return SignatureResult.success(sender);
        } else {
            return SignatureResult.failed("签名验证失败");
        }
    }

    /**
     * 兼容旧方法
     */
    public SignatureResult verifySignature(String senderId, long timestamp, String body, String signature) {
        return verifySignature(senderId, timestamp, signature);
    }

    /**
     * 验证签名并返回发送方身份
     */
    public Identity authenticate(String senderId, long timestamp, String signature) {
        SignatureResult result = verifySignature(senderId, timestamp, signature);
        if (result.isSuccess()) {
            return result.getIdentity();
        }
        throw new SecurityException(result.getErrorMessage());
    }

    // === 工具方法 ===

    private String sha256(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(SHA256);
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }

    private String hmacSha256(String key, String message) throws Exception {
        Mac mac = Mac.getInstance(HMAC_SHA256);
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
        mac.init(secretKey);
        byte[] hmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hmac);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 常量时间比较，防止时序攻击
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);

        // 使用 MessageDigest.isEqual 进行常量时间比较
        return MessageDigest.isEqual(aBytes, bBytes);
    }

    /**
     * 签名验证结果
     */
    public static class SignatureResult {
        private final boolean success;
        private final Identity identity;
        private final String errorMessage;

        private SignatureResult(boolean success, Identity identity, String errorMessage) {
            this.success = success;
            this.identity = identity;
            this.errorMessage = errorMessage;
        }

        public static SignatureResult success(Identity identity) {
            return new SignatureResult(true, identity, null);
        }

        public static SignatureResult failed(String errorMessage) {
            return new SignatureResult(false, null, errorMessage);
        }

        public boolean isSuccess() { return success; }
        public Identity getIdentity() { return identity; }
        public String getErrorMessage() { return errorMessage; }
    }
}
