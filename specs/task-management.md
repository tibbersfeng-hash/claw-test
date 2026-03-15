# 任务管理功能规范

## 1. 背景

为 claw-test 项目添加任务管理功能，通过 REST API 提供任务的增删改查能力。

## 2. 目标

- 提供完整的任务管理 REST API
- 使用 SQLite 作为数据存储
- 支持任务状态流转

## 3. 需求

### 3.1 功能需求

| 功能 | 描述 |
|------|------|
| 创建任务 | 提交任务内容和创建人 |
| 查询任务列表 | 支持分页和状态筛选 |
| 查询单个任务 | 根据 ID 获取任务详情 |
| 完成任务 | 将任务标记为已完成，可添加备注 |

### 3.2 任务字段

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 自动 | 主键，自增 |
| content | String | 是 | 任务内容，最大 1000 字符 |
| creator | String | 是 | 创建人，最大 100 字符 |
| status | Enum | 自动 | 状态：INIT, IN_PROGRESS, COMPLETED，默认 INIT |
| remark | String | 否 | 完成备注，最大 500 字符，完成任务时填写 |
| createdAt | DateTime | 自动 | 创建时间 |
| updatedAt | DateTime | 自动 | 修改时间 |

### 3.3 状态定义

| 状态 | 值 | 说明 |
|------|------|------|
| 初始化 | INIT | 任务刚创建 |
| 进行中 | IN_PROGRESS | 任务正在处理 |
| 已完成 | COMPLETED | 任务已完成 |

## 4. API 设计

### 4.1 创建任务

**请求**
```
POST /api/tasks
Content-Type: application/json
```

**请求参数 (Body)**

| 参数 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| content | String | 是 | 非空，最大1000字符 | 任务内容 |
| creator | String | 是 | 非空，最大100字符 | 创建人 |

**请求示例**
```json
{
  "content": "完成项目文档",
  "creator": "张三"
}
```

**响应参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 任务ID |
| content | String | 任务内容 |
| creator | String | 创建人 |
| status | String | 状态：INIT, IN_PROGRESS, COMPLETED |
| remark | String | 完成备注，未完成时为 null |
| createdAt | String | 创建时间，格式：yyyy-MM-ddTHH:mm:ss |
| updatedAt | String | 修改时间，格式：yyyy-MM-ddTHH:mm:ss |

**响应示例**
```json
// 201 Created
{
  "id": 1,
  "content": "完成项目文档",
  "creator": "张三",
  "status": "INIT",
  "remark": null,
  "createdAt": "2026-03-15T10:30:00",
  "updatedAt": "2026-03-15T10:30:00"
}
```

**错误响应**
```json
// 400 Bad Request - 参数校验失败
{
  "status": 400,
  "message": "参数校验失败",
  "errors": [
    "content: 不能为空",
    "creator: 不能为空"
  ]
}
```

---

### 4.2 查询任务列表

**请求**
```
GET /api/tasks
```

**请求参数 (Query)**

| 参数 | 类型 | 必填 | 默认值 | 校验规则 | 说明 |
|------|------|------|--------|----------|------|
| page | Integer | 否 | 0 | >= 0 | 页码，从0开始 |
| size | Integer | 否 | 10 | 1-100 | 每页数量 |
| status | String | 否 | null | INIT/IN_PROGRESS/COMPLETED | 状态筛选，不传则查询全部 |

**请求示例**
```
GET /api/tasks?page=0&size=10&status=INIT
```

**响应参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| content | Array | 任务列表 |
| content[].id | Long | 任务ID |
| content[].content | String | 任务内容 |
| content[].creator | String | 创建人 |
| content[].status | String | 状态 |
| content[].remark | String | 完成备注，未完成时为 null |
| content[].createdAt | String | 创建时间 |
| content[].updatedAt | String | 修改时间 |
| totalElements | Long | 总记录数 |
| totalPages | Integer | 总页数 |
| page | Integer | 当前页码 |
| size | Integer | 每页数量 |

**响应示例**
```json
// 200 OK
{
  "content": [
    {
      "id": 1,
      "content": "完成项目文档",
      "creator": "张三",
      "status": "INIT",
      "remark": null,
      "createdAt": "2026-03-15T10:30:00",
      "updatedAt": "2026-03-15T10:30:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "page": 0,
  "size": 10
}
```

---

### 4.3 查询单个任务

**请求**
```
GET /api/tasks/{id}
```

**请求参数 (Path)**

| 参数 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| id | Long | 是 | 正整数 | 任务ID |

**请求示例**
```
GET /api/tasks/1
```

**响应示例**
```json
// 200 OK
{
  "id": 1,
  "content": "完成项目文档",
  "creator": "张三",
  "status": "INIT",
  "remark": null,
  "createdAt": "2026-03-15T10:30:00",
  "updatedAt": "2026-03-15T10:30:00"
}
```

**错误响应**
```json
// 404 Not Found - 任务不存在
{
  "status": 404,
  "message": "任务不存在: id=1"
}
```

---

### 4.4 完成任务

**请求**
```
PUT /api/tasks/{id}/complete
Content-Type: application/json
```

**请求参数 (Path)**

| 参数 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| id | Long | 是 | 正整数 | 任务ID |

**请求参数 (Body)**

| 参数 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| remark | String | 否 | 最大500字符 | 完成备注 |

**请求示例**
```json
{
  "remark": "已完成文档编写，已提交审核"
}
```

**响应参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 任务ID |
| content | String | 任务内容 |
| creator | String | 创建人 |
| status | String | 状态：COMPLETED |
| remark | String | 完成备注 |
| createdAt | String | 创建时间 |
| updatedAt | String | 修改时间 |

**响应示例**
```json
// 200 OK
{
  "id": 1,
  "content": "完成项目文档",
  "creator": "张三",
  "status": "COMPLETED",
  "remark": "已完成文档编写，已提交审核",
  "createdAt": "2026-03-15T10:30:00",
  "updatedAt": "2026-03-15T11:00:00"
}
```

**错误响应**
```json
// 400 Bad Request - 任务已完成
{
  "status": 400,
  "message": "任务已完成，无法重复完成"
}

// 404 Not Found - 任务不存在
{
  "status": 404,
  "message": "任务不存在: id=1"
}
```

## 5. 技术设计

### 5.1 技术栈

| 组件 | 技术 |
|------|------|
| 框架 | Spring Boot 3.2.4 |
| 数据库 | SQLite |
| ORM | Spring Data JPA + Hibernate |
| 构建 | Maven |

### 5.2 依赖添加

```xml
<!-- SQLite JDBC -->
<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.45.1.0</version>
</dependency>

<!-- Spring Data JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

### 5.3 包结构

```
com.openclaw.test
├── controller
│   └── TaskController.java
├── service
│   └── TaskService.java
├── repository
│   └── TaskRepository.java
├── entity
│   ├── Task.java
│   └── TaskStatus.java
├── dto
│   ├── TaskCreateRequest.java
│   ├── TaskCompleteRequest.java
│   └── TaskResponse.java
└── exception
    ├── GlobalExceptionHandler.java
    └── TaskNotFoundException.java
```

### 5.4 数据库配置

```yaml
spring:
  datasource:
    url: jdbc:sqlite:/tmp/claw-test/tasks.db
    driver-class-name: org.sqlite.JDBC
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
```

## 6. 边界情况

| 场景 | 处理方式 |
|------|----------|
| 创建任务内容为空 | 返回 400 Bad Request |
| 查询不存在的任务 | 返回 404 Not Found |
| 完成不存在的任务 | 返回 404 Not Found |
| 重复完成任务 | 返回 400 Bad Request |
| 分页参数无效 | 使用默认值 page=0, size=10 |
| 状态筛选值无效 | 忽略筛选，返回所有任务 |

## 7. 验收标准

- [ ] POST /api/tasks 能创建任务，返回 201
- [ ] GET /api/tasks 能查询任务列表，支持分页
- [ ] GET /api/tasks?status=INIT 能按状态筛选
- [ ] GET /api/tasks/{id} 能查询单个任务
- [ ] PUT /api/tasks/{id}/complete 能完成任务并添加备注
- [ ] 数据持久化到 SQLite 文件
- [ ] 异常情况返回正确的 HTTP 状态码

## 8. 实现步骤

1. 添加 Maven 依赖
2. 配置 SQLite 数据源
3. 创建实体类 (Task, TaskStatus)
4. 创建 Repository
5. 创建 DTO 类
6. 创建 Service
7. 创建 Controller
8. 添加异常处理
9. 测试验证
