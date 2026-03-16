package com.openclaw.test.dto;

import jakarta.validation.constraints.Size;

public class DesignDocUpdateRequest {

    @Size(max = 200, message = "标题最大200字符")
    private String title;

    @Size(max = 10000, message = "内容最大10000字符")
    private String content;

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
}
