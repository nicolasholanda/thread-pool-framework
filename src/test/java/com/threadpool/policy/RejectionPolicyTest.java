package com.threadpool.policy;

import com.threadpool.exception.RejectedTaskException;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class RejectionPolicyTest {

    @Test
    void abortPolicyThrowsException() {
        RejectionPolicy policy = new AbortPolicy();
        assertThrows(RejectedTaskException.class, () -> policy.reject(() -> {}, "test-pool"));
    }

    @Test
    void callerRunsPolicyRunsTaskInline() {
        AtomicBoolean ran = new AtomicBoolean(false);
        RejectionPolicy policy = new CallerRunsPolicy();
        policy.reject(() -> ran.set(true), "test-pool");
        assertTrue(ran.get());
    }

    @Test
    void discardPolicyDropsTaskSilently() {
        AtomicBoolean ran = new AtomicBoolean(false);
        RejectionPolicy policy = new DiscardPolicy();
        assertDoesNotThrow(() -> policy.reject(() -> ran.set(true), "test-pool"));
        assertFalse(ran.get());
    }
}
