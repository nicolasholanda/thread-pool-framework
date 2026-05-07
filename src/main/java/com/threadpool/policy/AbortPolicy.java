package com.threadpool.policy;

import com.threadpool.exception.RejectedTaskException;

public class AbortPolicy implements RejectionPolicy {

    @Override
    public void reject(Runnable task, String poolName) {
        throw new RejectedTaskException("Task rejected by pool '" + poolName + "': queue is full");
    }
}
