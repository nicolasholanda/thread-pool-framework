package com.threadpool.queue;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class BoundedTaskQueueTest {

    @Test
    void offerAndPollTask() throws Exception {
        BoundedTaskQueue queue = new BoundedTaskQueue(10);
        Runnable task = () -> {};
        assertTrue(queue.offer(task));
        assertSame(task, queue.poll(100, TimeUnit.MILLISECONDS));
    }

    @Test
    void returnsFalseWhenFull() {
        BoundedTaskQueue queue = new BoundedTaskQueue(2);
        queue.offer(() -> {});
        queue.offer(() -> {});
        assertFalse(queue.offer(() -> {}));
    }

    @Test
    void sizeReflectsContents() {
        BoundedTaskQueue queue = new BoundedTaskQueue(10);
        assertEquals(0, queue.size());
        queue.offer(() -> {});
        queue.offer(() -> {});
        assertEquals(2, queue.size());
    }

    @Test
    void isEmptyWhenNoTasks() {
        BoundedTaskQueue queue = new BoundedTaskQueue(10);
        assertTrue(queue.isEmpty());
        queue.offer(() -> {});
        assertFalse(queue.isEmpty());
    }

    @Test
    void remainingCapacityDecreasesWithOffers() {
        BoundedTaskQueue queue = new BoundedTaskQueue(5);
        assertEquals(5, queue.remainingCapacity());
        queue.offer(() -> {});
        assertEquals(4, queue.remainingCapacity());
    }

    @Test
    void pollReturnsNullWhenEmpty() throws Exception {
        BoundedTaskQueue queue = new BoundedTaskQueue(10);
        assertNull(queue.poll(50, TimeUnit.MILLISECONDS));
    }
}
