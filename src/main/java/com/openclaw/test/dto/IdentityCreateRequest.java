package com.openclaw.test.dto;

import com.openclaw.test.entity.IdentityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class IdentityCreateRequest {

    @NotBlank(message = "身份名称不能为空")
    @Size(max = 100, message = "身份名称最大100字符")
    private String name;

    @NotNull(message = "身份类型不能为空")
    private IdentityType type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public IdentityType getType() {
        return type;
    }

    public void setType(IdentityType type) {
        this.type = type;
    }
}
