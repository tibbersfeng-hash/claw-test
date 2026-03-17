package com.openclaw.test.entity;

/**
 * 身份类型枚举
 *
 * PM - 产品经理：创建需求、验收、触发发布
 * UI - 设计师：生成设计稿、样式配置
 * DEV - 开发者：编码、修复Bug
 * QA - 测试工程师：执行测试、创建缺陷
 * OPS - 运维工程师：部署发布、监控
 */
public enum IdentityType {
    PM("产品经理"),
    UI("设计师"),
    DEV("开发者"),
    QA("测试工程师"),
    OPS("运维工程师");

    private final String displayName;

    IdentityType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
