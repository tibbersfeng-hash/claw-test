# Claw Test

OpenClaw 测试项目 - Spring Boot Web 服务

## 技术栈

- Java 21
- Spring Boot 3.2.4
- Spring Data JPA
- SQLite
- Maven

## 功能模块

### 身份管理
- 创建身份（自动生成 API Key）
- 查询身份列表
- 删除身份
- 重新生成 API Key

### 任务管理
- 创建任务（需要认证）
- 查询任务列表（分页、状态筛选）
- 修改任务内容
- 完成任务（可添加备注）
- 删除任务

## 运行方式

```bash
mvn spring-boot:run
```

服务启动后访问：
- 任务管理页面：http://localhost:8080/tasks.html
- 身份管理页面：http://localhost:8080/identities.html

## API 文档

详细接口文档请查看：[docs/api.md](docs/api.md)

### 快速开始

```bash
# 1. 创建身份获取 API Key
curl -X POST http://localhost:8080/api/identities \
  -H "Content-Type: application/json" \
  -d '{"type": "PM"}'

# 2. 使用 API Key 创建任务
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -H "X-API-Key: sk-xxx" \
  -d '{"content": "完成项目文档"}'

# 3. 查询任务列表
curl http://localhost:8080/api/tasks \
  -H "X-API-Key: sk-xxx"
```

## 身份类型

| 类型 | 说明 |
|------|------|
| PM | 产品经理 |
| OPS | 运维 |
| DEV | 开发 |

## 任务状态

| 状态 | 说明 |
|------|------|
| INIT | 初始化 |
| IN_PROGRESS | 进行中 |
| COMPLETED | 已完成 |

## 日志配置

| 配置项 | 值 |
|--------|-----|
| 日志文件路径 | `/tmp/claw-test/claw-test.log` |
| 日志保留天数 | 30 天 |
| 单个日志文件最大大小 | 10MB |

### 查看日志

```bash
tail -f /tmp/claw-test/claw-test.log
```

## 项目结构

```
claw-test/
├── docs/
│   └── api.md                    # API 接口文档
├── specs/
│   └── task-management.md        # 功能规范文档
├── src/main/
│   ├── java/com/openclaw/test/
│   │   ├── config/               # 配置类
│   │   ├── controller/           # 控制器
│   │   ├── dto/                  # 数据传输对象
│   │   ├── entity/               # 实体类
│   │   ├── exception/            # 异常处理
│   │   ├── repository/           # 数据访问层
│   │   └── service/              # 业务逻辑层
│   └── resources/
│       ├── application.yml       # 应用配置
│       └── static/               # 静态页面
├── pom.xml                       # Maven 配置
└── README.md                     # 项目说明
```

## 数据存储

- 数据库：SQLite
- 位置：`/tmp/claw-test/tasks.db`
