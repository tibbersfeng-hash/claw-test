package com.openclaw.test.exception;

public class IdentityNotFoundException extends RuntimeException {

    public IdentityNotFoundException(Long id) {
        super("身份不存在: id=" + id);
    }
}
