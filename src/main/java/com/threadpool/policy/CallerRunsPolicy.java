package com.threadpool.policy;

public class CallerRunsPolicy implements RejectionPolicy {

    @Override
    public void reject(Runnable task, String poolName) {
        task.run();
    }
}
