package com.openclaw.test.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RequirementCreateRequest {

    @NotBlank(message = "需求标题不能为空")
    @Size(max = 200, message = "需求标题最大200字符")
    private String title;

    @NotBlank(message = "需求内容不能为空")
    private String content;

    @Size(max = 50, message = "需求编号最大50字符")
    private String requirementNumber;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRequirementNumber() {
        return requirementNumber;
    }

    public void setRequirementNumber(String requirementNumber) {
        this.requirementNumber = requirementNumber;
    }
}
