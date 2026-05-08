package com.threadpool.config;

import com.threadpool.policy.RejectionPolicy;

import java.util.concurrent.TimeUnit;

public class ThreadPoolConfig {

    private final String name;
    private final int corePoolSize;
    private final int maxPoolSize;
    private final int queueCapacity;
    private final long keepAliveTime;
    private final TimeUnit keepAliveUnit;
    private final RejectionPolicy rejectionPolicy;

    public ThreadPoolConfig(String name, int corePoolSize, int maxPoolSize, int queueCapacity,
                            long keepAliveTime, TimeUnit keepAliveUnit, RejectionPolicy rejectionPolicy) {
        this.name = name;
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.queueCapacity = queueCapacity;
        this.keepAliveTime = keepAliveTime;
        this.keepAliveUnit = keepAliveUnit;
        this.rejectionPolicy = rejectionPolicy;
    }

    public String getName() { return name; }
    public int getCorePoolSize() { return corePoolSize; }
    public int getMaxPoolSize() { return maxPoolSize; }
    public int getQueueCapacity() { return queueCapacity; }
    public long getKeepAliveTime() { return keepAliveTime; }
    public TimeUnit getKeepAliveUnit() { return keepAliveUnit; }
    public RejectionPolicy getRejectionPolicy() { return rejectionPolicy; }
}
