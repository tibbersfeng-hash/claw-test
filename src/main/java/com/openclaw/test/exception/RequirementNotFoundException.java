package com.openclaw.test.exception;

public class RequirementNotFoundException extends RuntimeException {

    public RequirementNotFoundException(Long id) {
        super("需求文档不存在: id=" + id);
    }

    public RequirementNotFoundException(String requirementNumber) {
        super("需求文档不存在: requirementNumber=" + requirementNumber);
    }
}
