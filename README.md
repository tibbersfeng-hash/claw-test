# Claw Test

OpenClaw 测试项目 - Spring Boot Web 服务

## 技术栈

- Java 21
- Spring Boot 3.2.4
- Maven

## 运行方式

```bash
mvn spring-boot:run
```

## 接口

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /hello | 返回 Hello World |

## 示例

```bash
curl http://localhost:8080/hello
# 输出：Hello World
```

## 日志配置

| 配置项 | 值 |
|--------|-----|
| 日志文件路径 | `/tmp/claw-test/claw-test.log` |
| 日志保留天数 | 30 天 |
| 单个日志文件最大大小 | 10MB |
| 项目日志级别 | INFO |
| 包日志级别 | `com.openclaw: DEBUG` |

### 查看日志

```bash
# 实时查看日志
tail -f /tmp/claw-test/claw-test.log

# 查看最新 100 行
tail -n 100 /tmp/claw-test/claw-test.log
```

## 项目结构

```
claw-test/
├── src/main/
│   ├── java/com/openclaw/test/
│   │   ├── ClawTestApplication.java    # 主启动类
│   │   ├── config/WebConfig.java       # Web 配置
│   │   └── controller/HelloController.java  # 控制器
│   └── resources/
│       ├── application.yml             # 应用配置
│       └── static/index.html           # 静态首页
├── pom.xml                             # Maven 配置
└── README.md                           # 项目说明

日志文件：/tmp/claw-test/claw-test.log
```
