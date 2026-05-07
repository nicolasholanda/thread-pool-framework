package com.threadpool.exception;

public class RejectedTaskException extends RuntimeException {

    public RejectedTaskException(String message) {
        super(message);
    }
}
