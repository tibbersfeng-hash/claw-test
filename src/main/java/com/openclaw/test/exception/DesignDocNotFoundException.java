package com.openclaw.test.exception;

public class DesignDocNotFoundException extends RuntimeException {
    public DesignDocNotFoundException(Long id) {
        super("设计文档不存在: id=" + id);
    }
}
