package com.threadpool.core;

import com.threadpool.policy.RejectionPolicy;
import com.threadpool.queue.BoundedTaskQueue;

import java.util.concurrent.TimeUnit;

public class CachedThreadPool extends AbstractThreadPool {

    private final int minPoolSize;
    private final int maxPoolSize;
    private final long keepAliveTime;
    private final TimeUnit keepAliveUnit;

    public CachedThreadPool(String name, int minPoolSize, int maxPoolSize, int queueCapacity,
                            long keepAliveTime, TimeUnit keepAliveUnit, RejectionPolicy rejectionPolicy) {
        super(name, new BoundedTaskQueue(queueCapacity), rejectionPolicy);
        this.minPoolSize = minPoolSize;
        this.maxPoolSize = maxPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.keepAliveUnit = keepAliveUnit;
        for (int i = 0; i < minPoolSize; i++) {
            spawnWorker(keepAliveTime, keepAliveUnit);
        }
    }

    @Override
    protected boolean shouldWorkerExit(WorkerThread worker) {
        return getPoolSize() > minPoolSize;
    }

    @Override
    protected void onTaskEnqueued() {
        int current = getPoolSize();
        if (current < maxPoolSize && activeCount.get() >= current) {
            spawnWorker(keepAliveTime, keepAliveUnit);
        }
    }

    public int getMinPoolSize() {
        return minPoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }
}
