# API 接口文档

## 概述

- **Base URL**: `http://172.25.0.48:8080`
- **认证方式**: 任务管理接口需要在请求头中携带 `X-API-Key`
- **数据格式**: JSON

---

## 身份管理 API

身份管理接口用于管理 API Key，无需认证。

### 1. 创建身份

创建新的身份，自动生成 API Key。

**请求**

```
POST /api/identities
Content-Type: application/json
```

**请求体**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | string | 是 | 身份类型：PM / OPS / DEV |

**请求示例**

```json
{
  "type": "PM"
}
```

**响应示例**

```json
{
  "id": 1,
  "type": "PM",
  "apiKey": "sk-64900255a7114df1a65c16c7a2a36f68",
  "createdAt": "2026-03-15T17:30:31.513"
}
```

---

### 2. 查询身份列表

**请求**

```
GET /api/identities?page=0&size=10&type=PM
```

**查询参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | int | 否 | 0 | 页码，从 0 开始 |
| size | int | 否 | 10 | 每页数量，最大 100 |
| type | string | 否 | - | 类型筛选：PM / OPS / DEV |

**响应示例**

```json
{
  "content": [
    {
      "id": 1,
      "type": "PM",
      "apiKey": "sk-xxx",
      "createdAt": "2026-03-15T17:30:31.513"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 10
}
```

---

### 3. 查询单个身份

**请求**

```
GET /api/identities/{id}
```

**路径参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | 身份 ID |

---

### 4. 删除身份

**请求**

```
DELETE /api/identities/{id}
```

**响应**

- 204 No Content - 删除成功
- 404 Not Found - 身份不存在

---

### 5. 重新生成 API Key

**请求**

```
POST /api/identities/{id}/regenerate-key
```

**响应示例**

```json
{
  "id": 1,
  "type": "PM",
  "apiKey": "sk-new-api-key-here",
  "createdAt": "2026-03-15T17:30:31.513"
}
```

---

## 任务管理 API

任务管理接口需要认证，请在请求头中携带有效的 `X-API-Key`。

### 认证方式

```
X-API-Key: sk-xxx
```

**认证失败响应**

```json
{
  "status": 401,
  "message": "缺少认证头 X-API-Key"
}
```

---

### 1. 创建任务

**请求**

```
POST /api/tasks
Content-Type: application/json
X-API-Key: sk-xxx
```

**请求体**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| content | string | 是 | 任务内容，最大 1000 字符 |

**请求示例**

```json
{
  "content": "完成项目文档编写\n支持多行内容"
}
```

**响应示例**

```json
{
  "id": 1,
  "content": "完成项目文档编写\n支持多行内容",
  "creator": "PM-1",
  "handler": "DEV-1",
  "status": "INIT",
  "remark": null,
  "createdAt": "2026-03-15T17:30:52.156",
  "updatedAt": "2026-03-15T17:30:52.156"
}
```

**说明**

- `creator` 自动根据身份生成，格式：`{类型}-{ID}`，如 `PM-1`、`DEV-2`
- `handler` 当前处理人，PM创建任务时自动分配第一个DEV身份作为处理人

---

### 2. 查询任务列表

**请求**

```
GET /api/tasks?page=0&size=10&status=INIT&creatorType=PM&handlerType=DEV
X-API-Key: sk-xxx
```

**查询参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | int | 否 | 0 | 页码，从 0 开始 |
| size | int | 否 | 10 | 每页数量，最大 100 |
| status | string | 否 | - | 状态筛选：INIT / IN_PROGRESS / COMPLETED |
| creatorType | string | 否 | - | 创建者身份类型筛选：PM / OPS / DEV |
| handlerType | string | 否 | - | 处理人身份类型筛选：PM / OPS / DEV |

**响应示例**

```json
{
  "content": [
    {
      "id": 1,
      "content": "任务内容",
      "creator": "PM-1",
      "handler": "DEV-1",
      "status": "INIT",
      "remark": null,
      "createdAt": "2026-03-15T17:30:52.156",
      "updatedAt": "2026-03-15T17:30:52.156"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 10
}
```

---

### 3. 查询单个任务

**请求**

```
GET /api/tasks/{id}
X-API-Key: sk-xxx
```

**路径参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | 任务 ID |

---

### 4. 修改任务内容

**请求**

```
PUT /api/tasks/{id}
Content-Type: application/json
X-API-Key: sk-xxx
```

**请求体**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| content | string | 是 | 新的任务内容，最大 1000 字符 |

**请求示例**

```json
{
  "content": "修改后的任务内容"
}
```

---

### 5. 开始任务

将任务状态从"初始化"改为"进行中"。

**请求**

```
PUT /api/tasks/{id}/start
X-API-Key: sk-xxx
```

**响应示例**

```json
{
  "id": 1,
  "content": "任务内容",
  "creator": "PM-1",
  "handler": "DEV-1",
  "status": "IN_PROGRESS",
  "remark": null,
  "createdAt": "2026-03-15T17:30:52.156",
  "updatedAt": "2026-03-15T17:35:00.000"
}
```

**错误响应**

```json
{
  "status": 400,
  "message": "任务已完成，无法标记进行中"
}
```

---

### 6. 完成任务

**请求**

```
PUT /api/tasks/{id}/complete
Content-Type: application/json
X-API-Key: sk-xxx
```

**请求体**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| remark | string | 否 | 完成备注，最大 500 字符 |

**请求示例**

```json
{
  "remark": "任务已完成，测试通过"
}
```

**响应示例**

```json
{
  "id": 1,
  "content": "任务内容",
  "creator": "PM-1",
  "handler": "DEV-1",
  "status": "COMPLETED",
  "remark": "任务已完成，测试通过",
  "createdAt": "2026-03-15T17:30:52.156",
  "updatedAt": "2026-03-15T17:35:00.000"
}
```

**错误响应**

```json
{
  "status": 400,
  "message": "任务已完成，无法重复完成"
}
```

---

### 7. 删除任务

**请求**

```
DELETE /api/tasks/{id}
X-API-Key: sk-xxx
```

**响应**

- 204 No Content - 删除成功
- 404 Not Found - 任务不存在

---

## 数据模型

### 身份 (Identity)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 主键 |
| type | string | 类型：PM / OPS / DEV |
| apiKey | string | API Key，格式：sk-xxx |
| createdAt | datetime | 创建时间 |

### 任务 (Task)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 主键 |
| content | string | 任务内容 |
| creator | string | 创建人，格式：{类型}-{ID} |
| handler | string | 当前处理人，格式：{类型}-{ID} |
| status | string | 状态：INIT / IN_PROGRESS / COMPLETED |
| remark | string | 完成备注 |
| createdAt | datetime | 创建时间 |
| updatedAt | datetime | 修改时间 |

### 任务状态

| 状态 | 值 | 说明 |
|------|------|------|
| 初始化 | INIT | 任务刚创建 |
| 进行中 | IN_PROGRESS | 任务正在处理 |
| 已完成 | COMPLETED | 任务已完成 |

### 身份类型

| 类型 | 值 | 说明 |
|------|------|------|
| 产品经理 | PM | Product Manager |
| 运维 | OPS | Operations |
| 开发 | DEV | Developer |

---

## 项目管理 API

项目管理接口无需认证。

### 1. 创建项目

**请求**

```
POST /api/projects
Content-Type: application/json
```

**请求体**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | 是 | 项目名称，最大 100 字符 |
| repoUrl | string | 否 | 仓库地址，最大 500 字符 |
| projectPath | string | 否 | 项目路径，最大 500 字符 |

**请求示例**

```json
{
  "name": "claw-test",
  "repoUrl": "https://github.com/tibbersfeng-hash/claw-test.git",
  "projectPath": "/opt/codegspace/claw-test"
}
```

**响应示例**

```json
{
  "id": 1,
  "name": "claw-test",
  "repoUrl": "https://github.com/tibbersfeng-hash/claw-test.git",
  "projectPath": "/opt/codegspace/claw-test",
  "createdAt": "2026-03-15T22:50:00.000",
  "updatedAt": "2026-03-15T22:50:00.000"
}
```

---

### 2. 查询项目列表

**请求**

```
GET /api/projects?page=0&size=10&name=claw
```

**查询参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | int | 否 | 0 | 页码，从 0 开始 |
| size | int | 否 | 10 | 每页数量，最大 100 |
| name | string | 否 | - | 按名称模糊搜索 |

**响应示例**

```json
{
  "content": [
    {
      "id": 1,
      "name": "claw-test",
      "repoUrl": "https://github.com/tibbersfeng-hash/claw-test.git",
      "projectPath": "/opt/codegspace/claw-test",
      "createdAt": "2026-03-15T22:50:00.000",
      "updatedAt": "2026-03-15T22:50:00.000"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 10
}
```

---

### 3. 查询单个项目

**请求**

```
GET /api/projects/{id}
```

**路径参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | 项目 ID |

---

### 4. 更新项目

**请求**

```
PUT /api/projects/{id}
Content-Type: application/json
```

**请求体**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | 是 | 项目名称，最大 100 字符 |
| repoUrl | string | 否 | 仓库地址，最大 500 字符 |
| projectPath | string | 否 | 项目路径，最大 500 字符 |

**请求示例**

```json
{
  "name": "claw-test-v2",
  "repoUrl": "https://github.com/tibbersfeng-hash/claw-test.git",
  "projectPath": "/opt/codegspace/claw-test"
}
```

---

### 5. 删除项目

**请求**

```
DELETE /api/projects/{id}
```

**响应**

- 204 No Content - 删除成功
- 404 Not Found - 项目不存在

---

## 数据模型

### 项目 (Project)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 主键 |
| name | string | 项目名称 |
| repoUrl | string | 仓库地址 |
| projectPath | string | 项目路径 |
| createdAt | datetime | 创建时间 |
| updatedAt | datetime | 修改时间 |

---

## 错误响应

所有错误响应格式：

```json
{
  "status": 400,
  "message": "错误信息"
}
```

### 常见错误码

| 状态码 | 说明 |
|--------|------|
| 400 | 请求参数错误 |
| 401 | 认证失败（缺少或无效的 API Key） |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 使用示例

### cURL 示例

```bash
# 1. 创建身份
curl -X POST http://172.25.0.48:8080/api/identities \
  -H "Content-Type: application/json" \
  -d '{"type": "PM"}'

# 2. 创建任务
curl -X POST http://172.25.0.48:8080/api/tasks \
  -H "Content-Type: application/json" \
  -H "X-API-Key: sk-xxx" \
  -d '{"content": "完成项目文档"}'

# 3. 查询任务列表
curl http://172.25.0.48:8080/api/tasks \
  -H "X-API-Key: sk-xxx"

# 4. 完成任务
curl -X PUT http://172.25.0.48:8080/api/tasks/1/complete \
  -H "Content-Type: application/json" \
  -H "X-API-Key: sk-xxx" \
  -d '{"remark": "已完成"}'

# 5. 删除任务
curl -X DELETE http://172.25.0.48:8080/api/tasks/1 \
  -H "X-API-Key: sk-xxx"

# 6. 创建项目
curl -X POST http://172.25.0.48:8080/api/projects \
  -H "Content-Type: application/json" \
  -d '{"name": "my-project", "repoUrl": "https://github.com/user/repo.git", "projectPath": "/opt/codegspace/my-project"}'

# 7. 查询项目列表
curl http://172.25.0.48:8080/api/projects

# 8. 更新项目
curl -X PUT http://172.25.0.48:8080/api/projects/1 \
  -H "Content-Type: application/json" \
  -d '{"name": "my-project-v2", "repoUrl": "https://github.com/user/repo.git", "projectPath": "/opt/codegspace/my-project"}'

# 9. 删除项目
curl -X DELETE http://172.25.0.48:8080/api/projects/1
```
