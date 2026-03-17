package com.openclaw.test.dto;

import jakarta.validation.constraints.Size;

public class TaskCompleteRequest {

    @Size(max = 2000, message = "完成备注最大2000字符")
    private String remark;

    private String imageUrls;

    private String extraData;

    private Boolean createNextTask = true;

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(String imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }

    public Boolean getCreateNextTask() {
        return createNextTask;
    }

    public void setCreateNextTask(Boolean createNextTask) {
        this.createNextTask = createNextTask;
    }
}
