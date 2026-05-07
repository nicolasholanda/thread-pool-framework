package com.threadpool.core;

import com.threadpool.listener.TaskEvent;
import com.threadpool.listener.TaskEventType;
import com.threadpool.listener.TaskListener;
import com.threadpool.task.Task;
import com.threadpool.task.TaskFuture;
import com.threadpool.task.TaskStatus;

import java.util.List;

class TaskRunner<T> implements Runnable {

    private final Task<T> task;
    private final TaskFuture<T> future;
    private final List<TaskListener> listeners;
    private final AbstractThreadPool pool;

    TaskRunner(Task<T> task, TaskFuture<T> future, List<TaskListener> listeners, AbstractThreadPool pool) {
        this.task = task;
        this.future = future;
        this.listeners = listeners;
        this.pool = pool;
    }

    @Override
    public void run() {
        if (future.getStatus() == TaskStatus.CANCELLED) {
            return;
        }
        pool.activeCount.incrementAndGet();
        future.markRunning();
        fireEvent(TaskEvent.of(TaskEventType.STARTED, task.getName()));
        try {
            T result = task.getCallable().call();
            future.complete(result);
            fireEvent(TaskEvent.of(TaskEventType.COMPLETED, task.getName()));
        } catch (Throwable t) {
            future.fail(t);
            fireEvent(TaskEvent.failed(task.getName(), t));
        } finally {
            pool.activeCount.decrementAndGet();
            pool.completedTaskCount.incrementAndGet();
        }
    }

    private void fireEvent(TaskEvent event) {
        for (TaskListener listener : listeners) {
            try {
                listener.onEvent(event);
            } catch (Exception ignored) {
            }
        }
    }
}
