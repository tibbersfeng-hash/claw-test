# Agent 间通信认证机制设计

## 问题背景

在多 Agent 协作场景中，需要防止身份伪造：
- Agent A 与 Agent B 通信时
- Agent C 不能伪造成 Agent B 与 Agent A 通信

## 设计方案

### 核心原理

使用 **HMAC 签名** 机制验证发送方身份：
- 每个 Agent 拥有唯一的 API Key（作为私钥，只有自己和系统知道）
- 发送请求时，用 API Key 对关键信息签名
- 接收方通过系统验证签名，确认发送方身份

### 认证流程

```
┌─────────────────────────────────────────────────────────────────┐
│                    Agent A 发送请求给 Agent B                    │
├─────────────────────────────────────────────────────────────────┤
│  1. Agent A 构造请求:                                            │
│     - senderId: "PM-1"                                          │
│     - timestamp: 1711027200000                                  │
│     - body: {"task": "xxx", "action": "complete"}               │
│                                                                 │
│  2. Agent A 计算签名:                                            │
│     signature = HMAC-SHA256(apiKey, senderId + timestamp + hash(body)) │
│                                                                 │
│  3. 发送请求:                                                    │
│     Headers:                                                    │
│       X-Sender-Id: PM-1                                         │
│       X-Timestamp: 1711027200000                                │
│       X-Signature: abc123...                                    │
│       X-Target-Id: DEV-3                                        │
│     Body: {"task": "xxx", "action": "complete"}                 │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    系统验证请求                                  │
├─────────────────────────────────────────────────────────────────┤
│  1. 从 X-Sender-Id 获取声称的身份: "PM-1"                        │
│                                                                 │
│  2. 从数据库查询该身份的 API Key: sk-xxx                         │
│                                                                 │
│  3. 用 API Key 重新计算签名，与 X-Signature 对比                 │
│                                                                 │
│  4. 检查时间戳是否在有效范围内（5分钟内）                         │
│                                                                 │
│  5. 验证通过 → 确认发送方确实是 PM-1                             │
└─────────────────────────────────────────────────────────────────┘
```

### 为什么能防止伪造？

```
场景：Agent C 试图伪造成 Agent B 与 Agent A 通信

C 知道的信息：
- B 的 Identity ID: "DEV-3"（公开信息）
- 时间戳（可自己生成）
- 请求体内容

C 不知道的信息：
- B 的 API Key（只有 B 和系统知道）

没有 API Key，C 无法生成正确的签名：
signature = HMAC-SHA256(???, "DEV-3" + timestamp + bodyHash)
                    ↑
                    C 不知道这个值！

因此：
- 系统验证签名时会失败
- 请求被拒绝
- 身份伪造被阻止
```

## API 设计

### 请求头规范

| Header | 说明 | 示例 |
|--------|------|------|
| X-Sender-Id | 发送方身份标识 | PM-1 |
| X-Timestamp | 请求时间戳（毫秒） | 1711027200000 |
| X-Signature | HMAC-SHA256 签名 | abc123... |
| X-Target-Id | 目标接收方（可选） | DEV-3 |
| X-Nonce | 随机数（防重放，可选） | uuid |

### 签名算法

```java
public String generateSignature(String apiKey, String senderId, long timestamp, String body) {
    String bodyHash = DigestUtils.sha256Hex(body);
    String message = senderId + "|" + timestamp + "|" + bodyHash;
    return HmacUtils.hmacSha256Hex(apiKey, message);
}
```

### 验证逻辑

```java
public boolean verifySignature(String senderId, long timestamp, String body, String signature) {
    // 1. 检查时间戳（5分钟内有效）
    long now = System.currentTimeMillis();
    if (Math.abs(now - timestamp) > 5 * 60 * 1000) {
        return false; // 请求过期
    }

    // 2. 获取发送方的 API Key
    Identity sender = identityRepository.findByIdentityId(senderId);
    if (sender == null) {
        return false; // 发送方不存在
    }

    // 3. 计算期望签名
    String expectedSignature = generateSignature(sender.getApiKey(), senderId, timestamp, body);

    // 4. 常量时间比较（防止时序攻击）
    return MessageDigest.isEqual(
        expectedSignature.getBytes(),
        signature.getBytes()
    );
}
```

## 增强方案（可选）

### 1. Nonce 防重放

```java
// 存储 nonce，确保每个 nonce 只能使用一次
if (nonceRepository.exists(nonce)) {
    return false; // 重放攻击
}
nonceRepository.save(nonce, ttl = 5分钟);
```

### 2. 双向认证

Agent B 收到 Agent A 的请求后，可以验证：
```java
// B 回复 A 时，带上自己的签名
// A 可以验证回复确实来自 B
response.headers["X-Sender-Id"] = "DEV-3";
response.headers["X-Signature"] = sign(...);
```

### 3. 会话密钥

对于频繁通信的 Agent 对，可以建立会话密钥：
```java
// 首次通信时交换会话密钥
sessionKey = ECDH(A.privateKey, B.publicKey);
// 后续通信使用 sessionKey 签名（性能更好）
```

## 数据库变更

### Identity 表增强

```sql
ALTER TABLE identities ADD COLUMN identity_id VARCHAR(32) UNIQUE;
-- identity_id 如 "PM-1", "DEV-3"，用于标识身份

ALTER TABLE identities ADD COLUMN public_key TEXT;
-- 可选：存储公钥，用于非对称加密场景
```

### Nonce 记录表（可选）

```sql
CREATE TABLE nonces (
    nonce VARCHAR(64) PRIMARY KEY,
    sender_id VARCHAR(32),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 前端集成

Agent 在发送请求时：

```javascript
async function sendAgentRequest(targetId, body) {
    const senderId = getMyIdentityId();      // 如 "PM-1"
    const apiKey = getMyApiKey();            // 从本地存储获取
    const timestamp = Date.now();

    // 计算签名
    const bodyHash = await sha256(JSON.stringify(body));
    const message = `${senderId}|${timestamp}|${bodyHash}`;
    const signature = await hmacSha256(apiKey, message);

    // 发送请求
    const response = await fetch('/api/agent/message', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Sender-Id': senderId,
            'X-Timestamp': timestamp,
            'X-Signature': signature,
            'X-Target-Id': targetId
        },
        body: JSON.stringify(body)
    });

    return response;
}
```

## 安全性分析

| 攻击类型 | 防护措施 |
|---------|---------|
| 身份伪造 | 没有 API Key 无法生成有效签名 |
| 重放攻击 | 时间戳校验 + Nonce 机制 |
| 中间人攻击 | HTTPS + 签名验证 |
| 时序攻击 | 常量时间比较签名 |
| API Key 泄露 | 可定期轮换 Key |

## 实现优先级

1. **P0 - 核心签名验证**
   - 请求头规范
   - 签名生成和验证
   - 时间戳校验

2. **P1 - 增强安全**
   - Nonce 防重放
   - 常量时间比较

3. **P2 - 高级特性**
   - 会话密钥
   - 双向认证
   - Key 轮换机制
