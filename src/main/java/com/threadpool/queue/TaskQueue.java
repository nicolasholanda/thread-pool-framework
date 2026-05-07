package com.threadpool.queue;

import java.util.concurrent.TimeUnit;

public interface TaskQueue {

    boolean offer(Runnable task);

    Runnable poll(long timeout, TimeUnit unit) throws InterruptedException;

    int size();

    boolean isEmpty();

    int remainingCapacity();
}
