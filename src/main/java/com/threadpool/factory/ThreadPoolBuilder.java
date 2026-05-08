package com.threadpool.factory;

import com.threadpool.config.ThreadPoolConfig;
import com.threadpool.policy.AbortPolicy;
import com.threadpool.policy.RejectionPolicy;

import java.util.concurrent.TimeUnit;

public class ThreadPoolBuilder {

    private String name = "thread-pool";
    private int corePoolSize = Runtime.getRuntime().availableProcessors();
    private int maxPoolSize = Runtime.getRuntime().availableProcessors() * 2;
    private int queueCapacity = 100;
    private long keepAliveTime = 60;
    private TimeUnit keepAliveUnit = TimeUnit.SECONDS;
    private RejectionPolicy rejectionPolicy = new AbortPolicy();

    public ThreadPoolBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ThreadPoolBuilder corePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
        return this;
    }

    public ThreadPoolBuilder maxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
        return this;
    }

    public ThreadPoolBuilder queueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
        return this;
    }

    public ThreadPoolBuilder keepAlive(long time, TimeUnit unit) {
        this.keepAliveTime = time;
        this.keepAliveUnit = unit;
        return this;
    }

    public ThreadPoolBuilder rejectionPolicy(RejectionPolicy rejectionPolicy) {
        this.rejectionPolicy = rejectionPolicy;
        return this;
    }

    public ThreadPoolConfig build() {
        if (maxPoolSize < corePoolSize) {
            throw new IllegalArgumentException("maxPoolSize must be >= corePoolSize");
        }
        return new ThreadPoolConfig(name, corePoolSize, maxPoolSize, queueCapacity,
                keepAliveTime, keepAliveUnit, rejectionPolicy);
    }
}
