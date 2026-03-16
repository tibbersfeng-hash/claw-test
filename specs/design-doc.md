# 设计文档管理功能规范

## 1. 背景

为 claw-test 项目添加设计文档管理功能，将研发设计以 Markdown 格式持久化存储，便于追溯和查阅。

## 2. 目标

- 提供设计文档的 CRUD REST API
- 支持 Markdown 格式存储
- 与任务关联，便于追溯

## 3. 需求

### 3.1 功能需求

| 功能 | 描述 |
|------|------|
| 创建设计文档 | 独立 API 创建，或 DEV 完成任务时自动创建 |
| 查询设计文档列表 | 支持分页和任务ID筛选 |
| 查询单个设计文档 | 根据 ID 获取详情 |
| 更新设计文档 | 修改标题和内容 |
| 删除设计文档 | 根据 ID 删除 |

### 3.2 设计文档字段

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 自动 | 主键，自增 |
| title | String | 是 | 标题，最大 200 字符 |
| content | String | 是 | Markdown 内容，最大 10000 字符 |
| creator | String | 是 | 创建人，格式：{类型}-{ID} |
| taskId | Long | 否 | 关联任务ID |
| createdAt | DateTime | 自动 | 创建时间 |
| updatedAt | DateTime | 自动 | 修改时间 |

### 3.3 创建时机

1. **DEV 完成任务时自动创建**
   - 当 DEV 调用 `PUT /api/tasks/{id}/complete` 且有 `designContent` 时
   - 自动创建设计文档，关联当前任务
   - 标题格式：`任务#{taskId} 设计文档`

2. **独立 API 创建**
   - 任何已认证身份都可以创建
   - 可选关联任务ID

## 4. API 设计

### 4.1 创建设计文档

**请求**
```
POST /api/design-docs
Content-Type: application/json
X-API-Key: sk-xxx
```

**请求体**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | String | 是 | 标题，最大 200 字符 |
| content | String | 是 | Markdown 内容，最大 10000 字符 |
| taskId | Long | 否 | 关联任务ID |

**请求示例**
```json
{
  "title": "用户认证模块设计",
  "content": "# 用户认证模块\n\n## 概述\n...",
  "taskId": 1
}
```

**响应示例**
```json
{
  "id": 1,
  "title": "用户认证模块设计",
  "content": "# 用户认证模块\n\n## 概述\n...",
  "creator": "DEV-1",
  "taskId": 1,
  "createdAt": "2026-03-16T18:00:00",
  "updatedAt": "2026-03-16T18:00:00"
}
```

---

### 4.2 查询设计文档列表

**请求**
```
GET /api/design-docs?page=0&size=10&taskId=1
X-API-Key: sk-xxx
```

**查询参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | int | 否 | 0 | 页码，从 0 开始 |
| size | int | 否 | 10 | 每页数量，最大 100 |
| taskId | long | 否 | - | 按任务ID筛选 |

**响应示例**
```json
{
  "content": [
    {
      "id": 1,
      "title": "用户认证模块设计",
      "content": "...",
      "creator": "DEV-1",
      "taskId": 1,
      "createdAt": "2026-03-16T18:00:00",
      "updatedAt": "2026-03-16T18:00:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 10
}
```

---

### 4.3 查询单个设计文档

**请求**
```
GET /api/design-docs/{id}
X-API-Key: sk-xxx
```

---

### 4.4 更新设计文档

**请求**
```
PUT /api/design-docs/{id}
Content-Type: application/json
X-API-Key: sk-xxx
```

**请求体**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | String | 否 | 标题，最大 200 字符 |
| content | String | 否 | Markdown 内容，最大 10000 字符 |

**请求示例**
```json
{
  "title": "用户认证模块设计 v2",
  "content": "# 用户认证模块\n\n## 更新..."
}
```

---

### 4.5 删除设计文档

**请求**
```
DELETE /api/design-docs/{id}
X-API-Key: sk-xxx
```

**响应**
- 204 No Content - 删除成功
- 404 Not Found - 文档不存在

---

## 5. 技术设计

### 5.1 新增文件

```
src/main/java/com/openclaw/test/
├── entity/
│   └── DesignDoc.java
├── repository/
│   └── DesignDocRepository.java
├── service/
│   └── DesignDocService.java
├── controller/
│   └── DesignDocController.java
└── dto/
    ├── DesignDocCreateRequest.java
    ├── DesignDocUpdateRequest.java
    └── DesignDocResponse.java
```

### 5.2 修改文件

- `TaskService.java` - 完成任务时自动创建设计文档

### 5.3 数据库表

```sql
CREATE TABLE design_docs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    creator VARCHAR(100) NOT NULL,
    task_id INTEGER,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);
```

## 6. 边界情况

| 场景 | 处理方式 |
|------|----------|
| 创建时标题为空 | 返回 400 Bad Request |
| 内容超过 10000 字符 | 返回 400 Bad Request |
| 关联的任务不存在 | 允许，taskId 可为空或无效 |
| 更新不存在的文档 | 返回 404 Not Found |
| 删除不存在的文档 | 返回 404 Not Found |

## 7. 验收标准

- [ ] POST /api/design-docs 能创建设计文档
- [ ] GET /api/design-docs 能查询列表，支持分页和 taskId 筛选
- [ ] GET /api/design-docs/{id} 能查询详情
- [ ] PUT /api/design-docs/{id} 能更新文档
- [ ] DELETE /api/design-docs/{id} 能删除文档
- [ ] DEV 完成任务时自动创建设计文档
- [ ] 单元测试覆盖核心逻辑

## 8. 实现步骤

1. 创建实体类 DesignDoc
2. 创建 Repository
3. 创建 DTO 类
4. 创建 Service
5. 创建 Controller
6. 修改 TaskService，完成时自动创建设计文档
7. 编写单元测试
8. 测试验证
