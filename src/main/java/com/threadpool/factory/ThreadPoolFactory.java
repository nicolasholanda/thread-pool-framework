package com.threadpool.factory;

import com.threadpool.config.ThreadPoolConfig;
import com.threadpool.core.CachedThreadPool;
import com.threadpool.core.FixedThreadPool;
import com.threadpool.core.ThreadPool;
import com.threadpool.policy.AbortPolicy;

import java.util.concurrent.TimeUnit;

public class ThreadPoolFactory {

    private ThreadPoolFactory() {}

    public static ThreadPool fixedPool(ThreadPoolConfig config) {
        return new FixedThreadPool(config.getName(), config.getCorePoolSize(),
                config.getQueueCapacity(), config.getRejectionPolicy());
    }

    public static ThreadPool cachedPool(ThreadPoolConfig config) {
        return new CachedThreadPool(config.getName(), config.getCorePoolSize(),
                config.getMaxPoolSize(), config.getQueueCapacity(),
                config.getKeepAliveTime(), config.getKeepAliveUnit(),
                config.getRejectionPolicy());
    }

    public static ThreadPool fixedPool(String name, int poolSize) {
        return new FixedThreadPool(name, poolSize, poolSize * 10, new AbortPolicy());
    }

    public static ThreadPool cachedPool(String name, int minSize, int maxSize) {
        return new CachedThreadPool(name, minSize, maxSize, maxSize * 10,
                60, TimeUnit.SECONDS, new AbortPolicy());
    }
}
