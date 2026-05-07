package com.threadpool.core;

import com.threadpool.exception.RejectedTaskException;
import com.threadpool.listener.TaskEvent;
import com.threadpool.listener.TaskEventType;
import com.threadpool.listener.TaskListener;
import com.threadpool.policy.RejectionPolicy;
import com.threadpool.queue.TaskQueue;
import com.threadpool.task.Task;
import com.threadpool.task.TaskFuture;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractThreadPool implements ThreadPool {

    protected final String name;
    protected final TaskQueue taskQueue;
    protected final RejectionPolicy rejectionPolicy;
    protected final List<TaskListener> listeners = new CopyOnWriteArrayList<>();
    protected final Set<WorkerThread> workers = ConcurrentHashMap.newKeySet();
    protected volatile boolean shutdown = false;
    final AtomicInteger activeCount = new AtomicInteger(0);
    final AtomicLong completedTaskCount = new AtomicLong(0);
    private final AtomicInteger workerCounter = new AtomicInteger(0);

    protected AbstractThreadPool(String name, TaskQueue taskQueue, RejectionPolicy rejectionPolicy) {
        this.name = name;
        this.taskQueue = taskQueue;
        this.rejectionPolicy = rejectionPolicy;
    }

    @Override
    public <T> TaskFuture<T> submit(Task<T> task) {
        if (shutdown) {
            throw new RejectedTaskException("Pool '" + name + "' is shut down");
        }
        TaskFuture<T> future = new TaskFuture<>();
        TaskRunner<T> runner = new TaskRunner<>(task, future, listeners, this);
        if (taskQueue.offer(runner)) {
            fireEvent(TaskEvent.of(TaskEventType.SUBMITTED, task.getName()));
            onTaskEnqueued();
        } else {
            rejectionPolicy.reject(runner, name);
        }
        return future;
    }

    @Override
    public <T> TaskFuture<T> submit(Callable<T> callable) {
        return submit(Task.of(callable));
    }

    @Override
    public TaskFuture<Void> submit(Runnable runnable) {
        return submit(Task.of(runnable));
    }

    @Override
    public void addListener(TaskListener listener) {
        listeners.add(listener);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }

    @Override
    public int getPoolSize() {
        return workers.size();
    }

    @Override
    public int getActiveCount() {
        return activeCount.get();
    }

    @Override
    public long getCompletedTaskCount() {
        return completedTaskCount.get();
    }

    @Override
    public void shutdown() {
        shutdown = true;
    }

    @Override
    public void shutdownNow() {
        shutdown = true;
        for (WorkerThread worker : workers) {
            worker.stop();
        }
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        long deadline = System.nanoTime() + unit.toNanos(timeout);
        while (!workers.isEmpty()) {
            long remaining = deadline - System.nanoTime();
            if (remaining <= 0) {
                return false;
            }
            Thread.sleep(Math.min(50, TimeUnit.NANOSECONDS.toMillis(remaining) + 1));
        }
        return true;
    }

    boolean onWorkerIdle(WorkerThread worker) {
        if (shutdown) {
            return true;
        }
        return shouldWorkerExit(worker);
    }

    void onWorkerTerminated(WorkerThread worker) {
        workers.remove(worker);
    }

    protected WorkerThread spawnWorker(long keepAliveTime, TimeUnit unit) {
        String workerName = name + "-worker-" + workerCounter.incrementAndGet();
        WorkerThread worker = new WorkerThread(taskQueue, this, keepAliveTime, unit, workerName);
        workers.add(worker);
        worker.start();
        return worker;
    }

    protected void fireEvent(TaskEvent event) {
        for (TaskListener listener : listeners) {
            try {
                listener.onEvent(event);
            } catch (Exception ignored) {
            }
        }
    }

    protected abstract boolean shouldWorkerExit(WorkerThread worker);

    protected void onTaskEnqueued() {
    }
}
