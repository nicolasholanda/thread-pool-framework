package com.threadpool.core;

import com.threadpool.queue.TaskQueue;

import java.util.concurrent.TimeUnit;

class WorkerThread extends Thread {

    private final TaskQueue taskQueue;
    private final AbstractThreadPool pool;
    private final long keepAliveTime;
    private final TimeUnit keepAliveUnit;
    private volatile boolean running = true;

    WorkerThread(TaskQueue taskQueue, AbstractThreadPool pool, long keepAliveTime, TimeUnit keepAliveUnit, String name) {
        super(name);
        this.taskQueue = taskQueue;
        this.pool = pool;
        this.keepAliveTime = keepAliveTime;
        this.keepAliveUnit = keepAliveUnit;
        setDaemon(false);
    }

    @Override
    public void run() {
        try {
            while (running) {
                Runnable task = taskQueue.poll(keepAliveTime, keepAliveUnit);
                if (task != null) {
                    task.run();
                } else if (pool.onWorkerIdle(this)) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            pool.onWorkerTerminated(this);
        }
    }

    void stop() {
        running = false;
        interrupt();
    }
}
