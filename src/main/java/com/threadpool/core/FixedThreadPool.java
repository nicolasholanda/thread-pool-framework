package com.threadpool.core;

import com.threadpool.policy.RejectionPolicy;
import com.threadpool.queue.BoundedTaskQueue;

import java.util.concurrent.TimeUnit;

public class FixedThreadPool extends AbstractThreadPool {

    private final int poolSize;

    public FixedThreadPool(String name, int poolSize, int queueCapacity, RejectionPolicy rejectionPolicy) {
        super(name, new BoundedTaskQueue(queueCapacity), rejectionPolicy);
        this.poolSize = poolSize;
        for (int i = 0; i < poolSize; i++) {
            spawnWorker(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }
    }

    @Override
    protected boolean shouldWorkerExit(WorkerThread worker) {
        return false;
    }

    public int getConfiguredPoolSize() {
        return poolSize;
    }
}
