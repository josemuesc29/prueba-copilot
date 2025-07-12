package com.imaginamos.farmatodo.backend.order.create_order.infraestructure;

public class RetryFailedException extends RuntimeException {
    public RetryFailedException(String message, Throwable cause) {
        super(message, cause);
    }
    public RetryFailedException(String message) {
        super(message);
    }
}
