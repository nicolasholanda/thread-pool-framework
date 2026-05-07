package com.threadpool.policy;

public class DiscardPolicy implements RejectionPolicy {

    @Override
    public void reject(Runnable task, String poolName) {
    }
}
