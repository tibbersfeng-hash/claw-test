---
name: dev-task
description: Use when receiving development tasks - saves original requirements to task system before execution, then updates task with completion summary
---

# Development Task Tracking

## Overview

This skill ensures all development tasks are tracked in the task management system. When you receive a task:
1. Save the original requirement to the task system first
2. Record the task ID
3. Execute the task
4. Save completion summary to task remark and mark as completed

**IMPORTANT: Only create tasks for user requests. Do NOT create tasks for:**
- Your own testing
- Internal validation
- Self-initiated work

## API Configuration

- **Base URL**: `http://172.25.0.48:8080`
- **API Key**: 从环境变量 `TASK_API_KEY` 获取
- **Auth Header**: `X-API-Key: $TASK_API_KEY`

**Setup:** Set environment variable before use:
```bash
export TASK_API_KEY="your-api-key-here"
```

## Workflow

```dot
digraph dev_task {
    "Receive task request" [shape=doublecircle];
    "Create task in system" [shape=box];
    "Record task ID" [shape=box];
    "Execute task" [shape=box];
    "Task completed?" [shape=diamond];
    "Save summary to remark" [shape=box];
    "Mark task as COMPLETED" [shape=box];
    "Done" [shape=doublecircle];

    "Receive task request" -> "Create task in system";
    "Create task in system" -> "Record task ID";
    "Record task ID" -> "Execute task";
    "Execute task" -> "Task completed?";
    "Task completed?" -> "Save summary to remark" [label="yes"];
    "Task completed?" -> "Execute task" [label="no, continue"];
    "Save summary to remark" -> "Mark task as COMPLETED";
    "Mark task as COMPLETED" -> "Done";
}
```

## Step 1: Create Task

**Before starting any work**, create a task in the system:

```bash
curl -X POST http://172.25.0.48:8080/api/tasks \
  -H "Content-Type: application/json" \
  -H "X-API-Key: $TASK_API_KEY" \
  -d '{"content": "<original requirement text>"}'
```

**Response includes task ID:**
```json
{
  "id": 123,
  "content": "...",
  "creator": "PM-1",
  "status": "INIT",
  ...
}
```

**Record the task ID** for later use.

## Step 2: Execute Task

Perform the actual development work:
- Write code
- Run tests
- Fix bugs
- etc.

## Step 3: Complete Task

After finishing, update the task with completion summary:

```bash
curl -X PUT http://172.25.0.48:8080/api/tasks/{TASK_ID}/complete \
  -H "Content-Type: application/json" \
  -H "X-API-Key: $TASK_API_KEY" \
  -d '{"remark": "<completion summary>"}'
```

**Summary should include:**
- What was done
- Files modified
- Key changes made
- Any important notes

## Quick Reference

| Action | Method | Endpoint |
|--------|--------|----------|
| Create task | POST | `/api/tasks` |
| Get task | GET | `/api/tasks/{id}` |
| Complete task | PUT | `/api/tasks/{id}/complete` |
| List tasks | GET | `/api/tasks` |

## Example

**User request:** "Add a login button to the homepage"

**Step 1 - Create task:**
```bash
curl -X POST http://172.25.0.48:8080/api/tasks \
  -H "Content-Type: application/json" \
  -H "X-API-Key: $TASK_API_KEY" \
  -d '{"content": "Add a login button to the homepage"}'
# Response: {"id": 45, ...}
```

**Step 2 - Execute:**
- Modify `index.html` to add login button
- Add click handler in `app.js`
- Test the functionality

**Step 3 - Complete:**
```bash
curl -X PUT http://172.25.0.48:8080/api/tasks/45/complete \
  -H "Content-Type: application/json" \
  -H "X-API-Key: $TASK_API_KEY" \
  -d '{"remark": "Added login button to homepage.\n- Modified: index.html (added button element)\n- Modified: app.js (added click handler)\n- Tested: button shows login modal on click"}'
```

## Important Rules

1. **ALWAYS create task first** - Never skip this step
2. **Record task ID immediately** - You'll need it to complete the task
3. **Write meaningful summaries** - Future reference depends on good documentation
4. **Mark as completed when done** - Don't leave tasks in IN_PROGRESS state
5. **Only for user requests** - Never create tasks for your own testing or validation

---

## Heartbeat - 定时任务拉取

定时检查未完成的任务，每次拉取一个执行。

### 拉取逻辑

1. 查询状态为 `INIT` 的任务
2. 按创建时间升序，取最早的一条
3. 执行任务
4. 完成后标记为 `COMPLETED`

### API 调用

```bash
# 1. 查询最早未开始的任务
curl -s "http://172.25.0.48:8080/api/tasks?status=INIT&size=1" \
  -H "X-API-Key: $TASK_API_KEY"

# 2. 完成任务
curl -X PUT http://172.25.0.48:8080/api/tasks/{TASK_ID}/complete \
  -H "Content-Type: application/json" \
  -H "X-API-Key: $TASK_API_KEY" \
  -d '{"remark": "<completion summary>"}'
```

### 设置定时任务

使用 cc-connect 的 cron 功能：

```bash
cc-connect cron add --cron "*/5 * * * *" \
  --prompt "检查未完成任务：调用 GET /api/tasks?status=INIT&size=1 获取任务，如有任务则开始执行，完成后调用 /complete 接口" \
  --desc "Task Heartbeat"
```

**建议间隔**: 每 5 分钟检查一次

### Workflow (Heartbeat)

```dot
digraph heartbeat {
    "Heartbeat triggered" [shape=doublecircle];
    "Query INIT tasks" [shape=box];
    "Has task?" [shape=diamond];
    "Execute task" [shape=box];
    "Complete task" [shape=box];
    "Wait next heartbeat" [shape=doublecircle];

    "Heartbeat triggered" -> "Query INIT tasks";
    "Query INIT tasks" -> "Has task?";
    "Has task?" -> "Execute task" [label="yes"];
    "Has task?" -> "Wait next heartbeat" [label="no"];
    "Execute task" -> "Complete task";
    "Complete task" -> "Wait next heartbeat";
}
```
