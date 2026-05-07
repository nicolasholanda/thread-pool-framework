package com.threadpool.policy;

public interface RejectionPolicy {

    void reject(Runnable task, String poolName);
}
