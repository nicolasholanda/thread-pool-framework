package com.threadpool.task;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

public class TaskFuture<T> {

    private final CountDownLatch latch = new CountDownLatch(1);
    private final AtomicReference<TaskStatus> status = new AtomicReference<>(TaskStatus.PENDING);
    private volatile T result;
    private volatile Throwable exception;

    public T get() throws InterruptedException, ExecutionException {
        latch.await();
        return resolveResult();
    }

    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (!latch.await(timeout, unit)) {
            throw new TimeoutException("Task did not complete within the given timeout");
        }
        return resolveResult();
    }

    private T resolveResult() throws ExecutionException {
        if (status.get() == TaskStatus.FAILED) {
            throw new ExecutionException(exception);
        }
        if (status.get() == TaskStatus.CANCELLED) {
            throw new ExecutionException(new IllegalStateException("Task was cancelled"));
        }
        return result;
    }

    public boolean cancel() {
        if (status.compareAndSet(TaskStatus.PENDING, TaskStatus.CANCELLED)) {
            latch.countDown();
            return true;
        }
        return false;
    }

    public TaskStatus getStatus() {
        return status.get();
    }

    public boolean isDone() {
        TaskStatus s = status.get();
        return s == TaskStatus.COMPLETED || s == TaskStatus.FAILED || s == TaskStatus.CANCELLED;
    }

    public void complete(T value) {
        result = value;
        status.set(TaskStatus.COMPLETED);
        latch.countDown();
    }

    public void fail(Throwable t) {
        exception = t;
        status.set(TaskStatus.FAILED);
        latch.countDown();
    }

    public void markRunning() {
        status.compareAndSet(TaskStatus.PENDING, TaskStatus.RUNNING);
    }
}
