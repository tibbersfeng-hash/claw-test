package com.openclaw.test.entity;

/**
 * 任务状态枚举
 *
 * PENDING - 待处理：在待办队列中，等待 Agent 拉取
 * IN_PROGRESS - 处理中：Agent 已拉取，正在执行
 * COMPLETED - 已完成：执行成功，触发下一阶段
 * REJECTED - 已拒绝：验收不通过，需返工
 * CLOSED - 已关闭：流程结束
 */
public enum TaskStatus {
    PENDING("待处理"),
    IN_PROGRESS("处理中"),
    COMPLETED("已完成"),
    REJECTED("已拒绝"),
    CLOSED("已关闭");

    private final String displayName;

    TaskStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
