package com.threadpool.core;

import com.threadpool.listener.TaskListener;
import com.threadpool.task.Task;
import com.threadpool.task.TaskFuture;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public interface ThreadPool {

    <T> TaskFuture<T> submit(Task<T> task);

    <T> TaskFuture<T> submit(Callable<T> callable);

    TaskFuture<Void> submit(Runnable runnable);

    void addListener(TaskListener listener);

    void shutdown();

    void shutdownNow();

    boolean isShutdown();

    boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException;

    int getActiveCount();

    int getPoolSize();

    long getCompletedTaskCount();

    String getName();
}
