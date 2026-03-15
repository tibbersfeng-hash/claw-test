package com.openclaw.test.dto;

import jakarta.validation.constraints.Size;

public class TaskCompleteRequest {

    @Size(max = 500, message = "完成备注最大500字符")
    private String remark;

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
