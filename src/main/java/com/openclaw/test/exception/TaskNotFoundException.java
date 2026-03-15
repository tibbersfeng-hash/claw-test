package com.openclaw.test.exception;

public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(Long id) {
        super("任务不存在: id=" + id);
    }
}
