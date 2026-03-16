package com.openclaw.test.dto;

import jakarta.validation.constraints.Size;

public class TaskCompleteRequest {

    @Size(max = 500, message = "完成备注最大500字符")
    private String remark;

    @Size(max = 2000, message = "设计内容最大2000字符")
    private String designContent;

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getDesignContent() {
        return designContent;
    }

    public void setDesignContent(String designContent) {
        this.designContent = designContent;
    }
}
