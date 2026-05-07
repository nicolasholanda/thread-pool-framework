package com.threadpool.queue;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class BoundedTaskQueue implements TaskQueue {

    private final LinkedBlockingQueue<Runnable> queue;

    public BoundedTaskQueue(int capacity) {
        this.queue = new LinkedBlockingQueue<>(capacity);
    }

    @Override
    public boolean offer(Runnable task) {
        return queue.offer(task);
    }

    @Override
    public Runnable poll(long timeout, TimeUnit unit) throws InterruptedException {
        return queue.poll(timeout, unit);
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public int remainingCapacity() {
        return queue.remainingCapacity();
    }
}
