package com.threadpool.core;

import com.threadpool.factory.ThreadPoolFactory;
import com.threadpool.policy.AbortPolicy;
import com.threadpool.task.TaskFuture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class CachedThreadPoolTest {

    private ThreadPool pool;

    @AfterEach
    void tearDown() throws InterruptedException {
        if (pool != null) {
            pool.shutdownNow();
            pool.awaitTermination(2, TimeUnit.SECONDS);
        }
    }

    @Test
    void submitCallableAndGetResult() throws Exception {
        pool = ThreadPoolFactory.cachedPool("cached-test", 1, 4);
        TaskFuture<String> future = pool.submit(() -> "result");
        assertEquals("result", future.get(1, TimeUnit.SECONDS));
    }

    @Test
    void scalesUpUnderLoad() throws Exception {
        pool = new CachedThreadPool("scale-test", 1, 4, 20, 60, TimeUnit.SECONDS, new AbortPolicy());
        CountDownLatch started = new CountDownLatch(4);
        CountDownLatch release = new CountDownLatch(1);

        List<TaskFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            futures.add(pool.submit(() -> {
                started.countDown();
                release.await();
                return null;
            }));
        }

        assertTrue(started.await(2, TimeUnit.SECONDS), "all tasks should start");
        assertTrue(pool.getPoolSize() > 1, "should have spawned extra workers");

        release.countDown();
        for (TaskFuture<Void> f : futures) {
            f.get(2, TimeUnit.SECONDS);
        }
    }

    @Test
    void idleWorkersExitAfterKeepAlive() throws Exception {
        pool = new CachedThreadPool("shrink-test", 1, 4, 20, 300, TimeUnit.MILLISECONDS, new AbortPolicy());
        CountDownLatch release = new CountDownLatch(1);

        for (int i = 0; i < 3; i++) {
            pool.submit(() -> { release.await(); return null; });
        }
        Thread.sleep(100);
        release.countDown();

        Thread.sleep(800);
        assertTrue(pool.getPoolSize() <= 1, "excess workers should have exited");
    }

    @Test
    void shutdownNowStopsAllWorkers() throws Exception {
        pool = ThreadPoolFactory.cachedPool("shutdown-test", 2, 4);
        pool.submit(() -> { Thread.sleep(10000); return null; });
        Thread.sleep(50);
        pool.shutdownNow();
        assertTrue(pool.awaitTermination(2, TimeUnit.SECONDS));
        assertTrue(pool.isShutdown());
    }

    @Test
    void minPoolSizeWorkersAlwaysPresent() throws Exception {
        pool = new CachedThreadPool("min-test", 2, 4, 20, 300, TimeUnit.MILLISECONDS, new AbortPolicy());
        Thread.sleep(500);
        assertEquals(2, pool.getPoolSize());
    }
}
