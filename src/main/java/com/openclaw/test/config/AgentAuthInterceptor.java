package com.openclaw.test.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclaw.test.entity.Identity;
import com.openclaw.test.service.AgentSignatureService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Agent 间通信认证拦截器
 * 使用 HMAC 签名验证发送方身份，防止伪造
 */
@Component
public class AgentAuthInterceptor implements HandlerInterceptor {

    public static final String SENDER_IDENTITY_ATTRIBUTE = "senderIdentity";

    // 请求头名称
    public static final String HEADER_SENDER_ID = "X-Sender-Id";
    public static final String HEADER_TIMESTAMP = "X-Timestamp";
    public static final String HEADER_SIGNATURE = "X-Signature";
    public static final String HEADER_TARGET_ID = "X-Target-Id";
    public static final String HEADER_NONCE = "X-Nonce";

    private final AgentSignatureService signatureService;
    private final ObjectMapper objectMapper;

    public AgentAuthInterceptor(AgentSignatureService signatureService, ObjectMapper objectMapper) {
        this.signatureService = signatureService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 OPTIONS 请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 读取请求头
        String senderId = request.getHeader(HEADER_SENDER_ID);
        String timestampStr = request.getHeader(HEADER_TIMESTAMP);
        String signature = request.getHeader(HEADER_SIGNATURE);

        // 验证必要参数
        if (senderId == null || senderId.isEmpty()) {
            return sendError(response, 401, "缺少发送方身份标识: " + HEADER_SENDER_ID);
        }
        if (timestampStr == null || timestampStr.isEmpty()) {
            return sendError(response, 401, "缺少时间戳: " + HEADER_TIMESTAMP);
        }
        if (signature == null || signature.isEmpty()) {
            return sendError(response, 401, "缺少签名: " + HEADER_SIGNATURE);
        }

        // 解析时间戳
        long timestamp;
        try {
            timestamp = Long.parseLong(timestampStr);
        } catch (NumberFormatException e) {
            return sendError(response, 400, "无效的时间戳格式");
        }

        // 验证签名（不再需要读取请求体）
        try {
            AgentSignatureService.SignatureResult result =
                signatureService.verifySignature(senderId, timestamp, signature);

            if (result.isSuccess()) {
                // 将发送方身份存入 request 属性
                request.setAttribute(SENDER_IDENTITY_ATTRIBUTE, result.getIdentity());
                return true;
            } else {
                return sendError(response, 401, result.getErrorMessage());
            }
        } catch (Exception e) {
            return sendError(response, 500, "签名验证失败: " + e.getMessage());
        }
    }

    private boolean sendError(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> error = new HashMap<>();
        error.put("status", status);
        error.put("message", message);
        response.getWriter().write(objectMapper.writeValueAsString(error));
        return false;
    }

    private String readRequestBody(HttpServletRequest request) throws Exception {
        // 直接读取请求体
        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }
}
