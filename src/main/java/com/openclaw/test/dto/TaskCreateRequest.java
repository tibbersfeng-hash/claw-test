package com.openclaw.test.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TaskCreateRequest {

    @NotBlank(message = "任务内容不能为空")
    @Size(max = 1000, message = "任务内容最大1000字符")
    private String content;

    @NotBlank(message = "创建人不能为空")
    @Size(max = 100, message = "创建人最大100字符")
    private String creator;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }
}
