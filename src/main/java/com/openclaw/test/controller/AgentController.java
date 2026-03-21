package com.openclaw.test.controller;

import com.openclaw.test.config.AgentAuthInterceptor;
import com.openclaw.test.dto.AgentMessageRequest;
import com.openclaw.test.dto.AgentMessageResponse;
import com.openclaw.test.dto.SignatureRequest;
import com.openclaw.test.dto.SignatureResponse;
import com.openclaw.test.entity.Identity;
import com.openclaw.test.service.AgentSignatureService;
import com.openclaw.test.service.IdentityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Agent 间通信 API
 * 提供签名生成和验证功能
 */
@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final AgentSignatureService signatureService;
    private final IdentityService identityService;

    public AgentController(AgentSignatureService signatureService, IdentityService identityService) {
        this.signatureService = signatureService;
        this.identityService = identityService;
    }

    /**
     * 生成签名（供 Agent 调用）
     * 注意：此接口需要 API Key 认证
     *
     * POST /api/agent/sign
     * Header: X-API-Key: sk-xxx
     * Body: {"timestamp": 1711027200000, "body": "{...}"}
     */
    @PostMapping("/sign")
    public ResponseEntity<SignatureResponse> generateSignature(
            @Valid @RequestBody SignatureRequest request,
            HttpServletRequest httpRequest) {

        // 获取当前身份
        Identity identity = (Identity) httpRequest.getAttribute("identity");
        if (identity == null) {
            return ResponseEntity.status(401).build();
        }

        // 确保有 identityId
        String senderId = identity.getIdentityId();
        if (senderId == null || senderId.isEmpty()) {
            senderId = identity.getType().name() + "-" + identity.getId();
        }

        // 生成签名
        String signature = signatureService.generateSignature(
            identity.getApiKey(),
            senderId,
            request.getTimestamp(),
            request.getBody()
        );

        return ResponseEntity.ok(new SignatureResponse(
            senderId,
            request.getTimestamp(),
            signature
        ));
    }

    /**
     * Agent 发送消息给其他 Agent
     * 使用签名认证
     *
     * POST /api/agent/message
     * Headers:
     *   X-Sender-Id: PM-1
     *   X-Timestamp: 1711027200000
     *   X-Signature: abc123...
     *   X-Target-Id: DEV-3
     * Body: {"type": "task_assign", "payload": {...}}
     */
    @PostMapping("/message")
    public ResponseEntity<AgentMessageResponse> sendMessage(
            @Valid @RequestBody AgentMessageRequest message,
            HttpServletRequest httpRequest) {

        // 获取已验证的发送方身份
        Identity sender = (Identity) httpRequest.getAttribute(AgentAuthInterceptor.SENDER_IDENTITY_ATTRIBUTE);
        if (sender == null) {
            return ResponseEntity.status(401).build();
        }

        // 获取目标身份
        String targetId = httpRequest.getHeader(AgentAuthInterceptor.HEADER_TARGET_ID);
        if (targetId == null || targetId.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(new AgentMessageResponse(false, "缺少目标身份标识", null));
        }

        // 这里可以实现实际的消息处理逻辑
        // 例如：存储消息、通知目标 Agent 等

        String senderIdentityId = sender.getIdentityId() != null ?
            sender.getIdentityId() : sender.getType().name() + "-" + sender.getId();

        Map<String, Object> result = new HashMap<>();
        result.put("messageId", System.currentTimeMillis());
        result.put("sender", senderIdentityId);
        result.put("target", targetId);
        result.put("type", message.getType());
        result.put("delivered", true);

        return ResponseEntity.ok(new AgentMessageResponse(
            true,
            "消息已发送",
            result
        ));
    }

    /**
     * 验证签名（测试用）
     *
     * POST /api/agent/verify
     * Body: {"senderId": "PM-1", "timestamp": 1711027200000, "body": "...", "signature": "..."}
     */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifySignature(
            @RequestBody Map<String, Object> request) {

        String senderId = (String) request.get("senderId");
        Long timestamp = Long.parseLong(request.get("timestamp").toString());
        String body = (String) request.get("body");
        String signature = (String) request.get("signature");

        AgentSignatureService.SignatureResult result =
            signatureService.verifySignature(senderId, timestamp, body, signature);

        Map<String, Object> response = new HashMap<>();
        response.put("valid", result.isSuccess());
        if (result.isSuccess()) {
            response.put("identity", result.getIdentity().getIdentityId());
        } else {
            response.put("error", result.getErrorMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 获取当前身份信息
     *
     * GET /api/agent/me
     * Header: X-API-Key: sk-xxx
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentAgent(HttpServletRequest httpRequest) {
        Identity identity = (Identity) httpRequest.getAttribute("identity");
        if (identity == null) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", identity.getId());
        response.put("type", identity.getType().name());
        response.put("identityId", identity.getIdentityId() != null ?
            identity.getIdentityId() : identity.getType().name() + "-" + identity.getId());
        response.put("apiKey", identity.getApiKey().substring(0, 10) + "..."); // 只显示前10位

        return ResponseEntity.ok(response);
    }
}
