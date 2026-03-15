# Specs 目录

本目录用于存放项目的规范文档（Specification）。

## Spec-Kit 流程

```
Plan → Spec → Implement → Verify
```

## 目录结构

```
specs/
├── README.md           # 本文件
├── template.md         # 规范模板
└── <feature-name>.md   # 具体功能规范
```

## 如何使用

1. 创建新功能规范前，先复制 `template.md`
2. 填写规范内容
3. 与团队确认规范
4. 按规范实现代码
5. 完成后标记规范状态

## 规范状态

| 状态 | 说明 |
|------|------|
| `draft` | 草稿阶段 |
| `review` | 待审查 |
| `approved` | 已批准，可实现 |
| `implementing` | 正在实现 |
| `done` | 已完成 |

## 命名规范

- 文件名使用小写，单词间用 `-` 连接
- 例如：`user-login.md`、`api-error-handling.md`
