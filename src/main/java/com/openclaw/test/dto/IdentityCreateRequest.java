package com.openclaw.test.dto;

import com.openclaw.test.entity.IdentityType;
import jakarta.validation.constraints.NotNull;

public class IdentityCreateRequest {

    @NotNull(message = "身份类型不能为空")
    private IdentityType type;

    public IdentityType getType() {
        return type;
    }

    public void setType(IdentityType type) {
        this.type = type;
    }
}
