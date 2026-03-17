# 工单系统实现计划

## 1. 背景

基于现有 claw-test 项目，扩展任务系统以支持 AI Agent 驱动的研发流程。实现五种工单类型（REQ/DES/DEV/BUG/REL）及其流转逻辑。

## 2. 目标

- 支持五种工单类型的创建、查询、状态流转
- 提供 Agent 任务拉取 API
- 支持飞书图片链接存储
- 实现工单间的关联与自动流转

## 3. 现有系统分析

### 3.1 已有能力
| 组件 | 状态 | 说明 |
|------|------|------|
| Spring Boot 3.2.4 | ✅ | 已配置 |
| SQLite 数据库 | ✅ | 已配置 |
| API Key 认证 | ✅ | Identity 实体 |
| IdentityType | ✅ | PM, OPS, DEV, QA |
| Task 实体 | ✅ | 基础任务模型 |
| Project 实体 | ✅ | 项目管理 |

### 3.2 需要新增
| 组件 | 说明 |
|------|------|
| TicketType 枚举 | REQ, DES, DEV, BUG, REL |
| TicketStatus 枚举 | 各工单类型的状态 |
| Ticket 实体 | 扩展 Task，支持工单类型 |
| 图片链接字段 | feishuImageUrls |
| 工单关联字段 | parentTicketId, relatedTickets |
| Agent API | 任务拉取、状态上报 |

## 4. 数据模型设计

### 4.1 工单类型 (TicketType)

```java
public enum TicketType {
    REQ,  // 需求工单
    DES,  // 设计工单
    DEV,  // 开发工单
    BUG,  // 缺陷工单
    REL   // 发布工单
}
```

### 4.2 工单状态 (TicketStatus)

每种工单类型有独立的状态流转：

| 工单类型 | 状态流转 |
|----------|----------|
| REQ | CREATED → IN_REVIEW → APPROVED → COMPLETED |
| DES | CREATED → DESIGNING → REVIEWING → COMPLETED |
| DEV | CREATED → CODING → CR_REVIEWING → TESTING → COMPLETED |
| BUG | CREATED → FIXING → VERIFYING → CLOSED |
| REL | CREATED → DEPLOYING → MONITORING → COMPLETED → ROLLED_BACK |

### 4.3 Ticket 实体扩展

在现有 Task 基础上新增字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| ticketType | TicketType | 工单类型 |
| ticketStatus | String | 工单状态（字符串存储，支持不同类型不同状态） |
| priority | Integer | 优先级 1-5 |
| feishuImageUrls | String | 飞书图片链接（JSON数组） |
| parentTicketId | Long | 父工单ID |
| designConfig | String | 设计配置（JSON） |
| codeBranch | String | 代码分支 |
| testResult | String | 测试结果 |
| errorInfo | String | 错误信息 |
| rollbackReason | String | 回滚原因 |

### 4.4 工单关联表 (TicketRelation)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| sourceTicketId | Long | 源工单ID |
| targetTicketId | Long | 目标工单ID |
| relationType | String | 关联类型：CREATES, BLOCKS, DEPENDS_ON |

## 5. API 设计

### 5.1 Agent 任务拉取 API

```
GET /api/agent/tasks?identityType={type}&status={status}
```

**请求参数**
| 参数 | 类型 | 说明 |
|------|------|------|
| identityType | String | Agent类型：PM, UI, DEV, QA, OPS |
| status | String | 状态筛选 |

**响应示例**
```json
{
  "tasks": [
    {
      "id": 1,
      "ticketType": "REQ",
      "ticketStatus": "CREATED",
      "content": "实现用户登录功能",
      "feishuImageUrls": ["https://feishu.cn/file/xxx"],
      "priority": 3
    }
  ]
}
```

### 5.2 工单创建 API

```
POST /api/tickets
```

**请求示例**
```json
{
  "ticketType": "REQ",
  "content": "实现用户登录功能",
  "priority": 3,
  "projectId": 1,
  "feishuImageUrls": ["https://feishu.cn/file/xxx"]
}
```

### 5.3 工单状态流转 API

```
PUT /api/tickets/{id}/transition
```

**请求示例**
```json
{
  "targetStatus": "APPROVED",
  "remark": "需求已评审通过",
  "feishuImageUrls": ["https://feishu.cn/file/yyy"]
}
```

### 5.4 工单完成并创建下游 API

```
PUT /api/tickets/{id}/complete
```

**请求示例**
```json
{
  "remark": "设计稿已完成",
  "feishuImageUrls": ["https://feishu.cn/file/design.png"],
  "designConfig": {
    "colors": {"primary": "#1890ff"},
    "fonts": {"main": "PingFang SC"}
  },
  "createDownstream": true
}
```

## 6. 工单流转规则

### 6.1 REQ → DES 流转

```
REQ(COMPLETED) → 自动创建 DES(CREATED)
```

PM Agent 完成需求工单后，系统自动创建设计工单。

### 6.2 DES → DEV 流转

```
DES(COMPLETED) → 自动创建 DEV(CREATED)
```

UI Agent 完成设计工单后，系统自动创建开发工单。

### 6.3 DEV → TEST/BUG 流转

```
DEV(TESTING) → Test Agent 执行测试
  ├─ 通过 → DEV(COMPLETED) → 创建 REL(CREATED)
  └─ 失败 → 创建 BUG(CREATED)
```

### 6.4 BUG → DEV 流转

```
BUG(CREATED) → 通知 Dev Agent 修复
BUG(VERIFYING) → Test Agent 验证
  ├─ 通过 → BUG(CLOSED)
  └─ 失败 → BUG(FIXING)
```

### 6.5 REL 流转

```
REL(CREATED) → DEPLOYING → MONITORING
  ├─ 成功 → COMPLETED
  └─ 失败 → ROLLED_BACK
```

## 7. 实现步骤

### Phase 1: 数据模型扩展 (Day 1)

1. 创建 TicketType 枚举
2. 创建 TicketStatus 枚举（或使用字符串）
3. 扩展 Task 实体为 Ticket 实体
4. 创建 TicketRelation 实体
5. 创建数据库迁移脚本

### Phase 2: 基础 API (Day 2)

1. 创建 TicketRepository
2. 创建 TicketService
3. 创建 TicketController
4. 实现工单 CRUD API

### Phase 3: 流转逻辑 (Day 3)

1. 实现状态机服务
2. 实现流转规则
3. 实现自动创建下游工单
4. 实现工单关联查询

### Phase 4: Agent API (Day 4)

1. 实现 Agent 任务拉取 API
2. 实现任务认领机制
3. 实现状态上报 API
4. 实现飞书图片链接管理

### Phase 5: 测试与文档 (Day 5)

1. 编写单元测试
2. 编写集成测试
3. 更新 API 文档
4. 验收测试

## 8. 文件清单

### 8.1 新增文件

```
src/main/java/com/openclaw/test/
├── entity/
│   ├── Ticket.java           # 扩展的工单实体
│   ├── TicketType.java       # 工单类型枚举
│   └── TicketRelation.java   # 工单关联
├── repository/
│   └── TicketRepository.java
├── service/
│   ├── TicketService.java
│   └── TicketFlowService.java  # 流转服务
├── controller/
│   └── TicketController.java
├── dto/
│   ├── TicketCreateRequest.java
│   ├── TicketTransitionRequest.java
│   └── TicketResponse.java
└── exception/
    └── InvalidTransitionException.java
```

### 8.2 修改文件

```
src/main/java/com/openclaw/test/
├── entity/
│   └── IdentityType.java     # 新增 UI Agent 类型
└── config/
    └── WebConfig.java        # 如需调整
```

## 9. 风险与缓解

| 风险 | 缓解措施 |
|------|----------|
| 数据库迁移 | 使用 SQLite 的 ALTER TABLE，保留现有数据 |
| 状态流转复杂 | 使用状态机模式，集中管理流转规则 |
| Agent 并发拉取 | 实现乐观锁，使用 version 字段 |
| 飞书 API 依赖 | 预留接口，先实现 Mock 实现 |

## 10. 验收标准

- [ ] 支持 5 种工单类型的创建
- [ ] 工单状态流转正确
- [ ] 完成工单后自动创建下游工单
- [ ] Agent 可以拉取待办任务
- [ ] 飞书图片链接正确存储和返回
- [ ] 工单关联关系正确维护
- [ ] 所有 API 返回正确的状态码
