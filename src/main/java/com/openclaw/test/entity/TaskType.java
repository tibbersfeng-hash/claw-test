package com.openclaw.test.entity;

/**
 * 任务类型枚举
 *
 * REQ - 需求任务：PM创建，包含需求描述、功能列表、优先级
 * DES - 设计任务：UI生成，包含设计稿链接、样式配置
 * DEV - 开发任务：DEV执行，包含代码分支、实现说明
 * TEST - 测试任务：QA执行，包含测试用例、测试结果
 * BUG - 缺陷任务：QA创建，包含错误信息、截图链接
 * ACCEPT - 验收任务：PM执行，包含验收结果
 * REL - 发布任务：OPS执行，包含发布记录、监控图表
 */
public enum TaskType {
    REQ("需求任务", "PM", "DES"),
    DES("设计任务", "UI", "DEV"),
    DEV("开发任务", "DEV", "TEST"),
    TEST("测试任务", "QA", "ACCEPT"),
    BUG("缺陷任务", "DEV", "TEST"),
    ACCEPT("验收任务", "PM", "REL"),
    REL("发布任务", "OPS", null);

    private final String displayName;
    private final String defaultHandlerRole;
    private final String nextTaskType;

    TaskType(String displayName, String defaultHandlerRole, String nextTaskType) {
        this.displayName = displayName;
        this.defaultHandlerRole = defaultHandlerRole;
        this.nextTaskType = nextTaskType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDefaultHandlerRole() {
        return defaultHandlerRole;
    }

    public String getNextTaskType() {
        return nextTaskType;
    }

    /**
     * 获取下一阶段任务类型
     */
    public TaskType getNextTaskTypeEnum() {
        if (nextTaskType == null) {
            return null;
        }
        try {
            return TaskType.valueOf(nextTaskType);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
