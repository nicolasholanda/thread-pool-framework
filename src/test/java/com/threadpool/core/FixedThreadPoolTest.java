package com.threadpool.core;

import com.threadpool.exception.RejectedTaskException;
import com.threadpool.listener.TaskEventType;
import com.threadpool.policy.AbortPolicy;
import com.threadpool.task.TaskFuture;
import com.threadpool.task.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class FixedThreadPoolTest {

    private ThreadPool pool;

    @BeforeEach
    void setUp() {
        pool = new FixedThreadPool("test-pool", 2, 10, new AbortPolicy());
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        pool.shutdownNow();
        pool.awaitTermination(2, TimeUnit.SECONDS);
    }

    @Test
    void submitCallableAndGetResult() throws Exception {
        TaskFuture<Integer> future = pool.submit(() -> 42);
        assertEquals(42, future.get(1, TimeUnit.SECONDS));
        assertEquals(TaskStatus.COMPLETED, future.getStatus());
    }

    @Test
    void submitRunnableCompletes() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);
        TaskFuture<Void> future = pool.submit(() -> counter.incrementAndGet());
        future.get(1, TimeUnit.SECONDS);
        assertEquals(1, counter.get());
    }

    @Test
    void submitsMultipleTasksConcurrently() throws Exception {
        List<TaskFuture<Integer>> futures = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            final int val = i;
            futures.add(pool.submit(() -> val * 2));
        }
        for (int i = 0; i < 5; i++) {
            assertEquals(i * 2, futures.get(i).get(2, TimeUnit.SECONDS));
        }
    }

    @Test
    void failedTaskSetsExceptionOnFuture() {
        TaskFuture<Void> future = pool.submit((Runnable) () -> { throw new RuntimeException("error"); });
        ExecutionException ex = assertThrows(ExecutionException.class,
                () -> future.get(1, TimeUnit.SECONDS));
        assertEquals("error", ex.getCause().getMessage());
        assertEquals(TaskStatus.FAILED, future.getStatus());
    }

    @Test
    void abortPolicyRejectsWhenQueueFull() throws InterruptedException {
        ThreadPool tinyPool = new FixedThreadPool("tiny", 1, 1, new AbortPolicy());
        CountDownLatch block = new CountDownLatch(1);
        tinyPool.submit(() -> { block.await(); return null; });
        Thread.sleep(50);
        tinyPool.submit(() -> null);
        assertThrows(RejectedTaskException.class, () -> tinyPool.submit(() -> null));
        block.countDown();
        tinyPool.shutdownNow();
    }

    @Test
    void listenerReceivesEvents() throws Exception {
        List<TaskEventType> events = new CopyOnWriteArrayList<>();
        pool.addListener(event -> events.add(event.getType()));

        pool.submit(() -> "done").get(1, TimeUnit.SECONDS);
        Thread.sleep(50);

        assertTrue(events.contains(TaskEventType.SUBMITTED));
        assertTrue(events.contains(TaskEventType.STARTED));
        assertTrue(events.contains(TaskEventType.COMPLETED));
    }

    @Test
    void shutdownPreventsNewTasks() {
        pool.shutdown();
        assertTrue(pool.isShutdown());
        assertThrows(RejectedTaskException.class, () -> pool.submit(() -> "late"));
    }

    @Test
    void completedTaskCountIncrements() throws Exception {
        pool.submit(() -> 1).get(1, TimeUnit.SECONDS);
        pool.submit(() -> 2).get(1, TimeUnit.SECONDS);
        assertEquals(2, pool.getCompletedTaskCount());
    }

    @Test
    void poolSizeMatchesConfiguration() {
        assertEquals(2, pool.getPoolSize());
    }
}
