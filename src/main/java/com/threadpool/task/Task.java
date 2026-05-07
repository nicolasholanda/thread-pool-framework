package com.threadpool.task;

import java.util.concurrent.Callable;

public class Task<T> {

    private final Callable<T> callable;
    private final String name;

    private Task(Callable<T> callable, String name) {
        this.callable = callable;
        this.name = name;
    }

    public static <T> Task<T> of(Callable<T> callable) {
        return new Task<>(callable, "task-" + System.nanoTime());
    }

    public static <T> Task<T> of(Callable<T> callable, String name) {
        return new Task<>(callable, name);
    }

    public static Task<Void> of(Runnable runnable) {
        return new Task<>(() -> { runnable.run(); return null; }, "task-" + System.nanoTime());
    }

    public static Task<Void> of(Runnable runnable, String name) {
        return new Task<>(() -> { runnable.run(); return null; }, name);
    }

    public Callable<T> getCallable() {
        return callable;
    }

    public String getName() {
        return name;
    }
}
