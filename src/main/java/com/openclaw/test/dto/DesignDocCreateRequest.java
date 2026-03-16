package com.openclaw.test.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DesignDocCreateRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题最大200字符")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Size(max = 10000, message = "内容最大10000字符")
    private String content;

    private Long taskId;

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

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }
}
