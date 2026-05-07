package com.threadpool.listener;

import java.time.Instant;

public class TaskEvent {

    private final TaskEventType type;
    private final String taskName;
    private final Instant timestamp;
    private final Throwable error;

    private TaskEvent(TaskEventType type, String taskName, Throwable error) {
        this.type = type;
        this.taskName = taskName;
        this.timestamp = Instant.now();
        this.error = error;
    }

    public static TaskEvent of(TaskEventType type, String taskName) {
        return new TaskEvent(type, taskName, null);
    }

    public static TaskEvent failed(String taskName, Throwable error) {
        return new TaskEvent(TaskEventType.FAILED, taskName, error);
    }

    public TaskEventType getType() {
        return type;
    }

    public String getTaskName() {
        return taskName;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Throwable getError() {
        return error;
    }
}
