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
# 输出: Hello World
```