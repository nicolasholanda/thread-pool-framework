package com.threadpool.task;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

class TaskFutureTest {

    @Test
    void completesAndReturnsResult() throws Exception {
        TaskFuture<String> future = new TaskFuture<>();
        future.complete("hello");
        assertEquals("hello", future.get());
        assertEquals(TaskStatus.COMPLETED, future.getStatus());
        assertTrue(future.isDone());
    }

    @Test
    void failsAndThrowsExecutionException() {
        TaskFuture<String> future = new TaskFuture<>();
        future.fail(new RuntimeException("boom"));
        ExecutionException ex = assertThrows(ExecutionException.class, future::get);
        assertEquals("boom", ex.getCause().getMessage());
        assertEquals(TaskStatus.FAILED, future.getStatus());
    }

    @Test
    void cancelsPendingFuture() throws Exception {
        TaskFuture<String> future = new TaskFuture<>();
        assertTrue(future.cancel());
        assertEquals(TaskStatus.CANCELLED, future.getStatus());
        assertTrue(future.isDone());
        assertThrows(ExecutionException.class, future::get);
    }

    @Test
    void cancelReturnsFalseIfAlreadyCompleted() {
        TaskFuture<String> future = new TaskFuture<>();
        future.complete("done");
        assertFalse(future.cancel());
    }

    @Test
    void getTimesOut() {
        TaskFuture<String> future = new TaskFuture<>();
        assertThrows(TimeoutException.class, () -> future.get(50, TimeUnit.MILLISECONDS));
    }

    @Test
    void markRunningTransitionsStatus() {
        TaskFuture<String> future = new TaskFuture<>();
        assertEquals(TaskStatus.PENDING, future.getStatus());
        future.markRunning();
        assertEquals(TaskStatus.RUNNING, future.getStatus());
    }

    @Test
    void getBlocksUntilCompleted() throws Exception {
        TaskFuture<Integer> future = new TaskFuture<>();
        Thread completer = new Thread(() -> {
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            future.complete(42);
        });
        completer.start();
        assertEquals(42, future.get(500, TimeUnit.MILLISECONDS));
        completer.join();
    }
}
