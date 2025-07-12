package com.imaginamos.farmatodo.backend.order.create_order.infraestructure;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Logger;


public class RetryUtil {

    private RetryUtil() {
        throw new IllegalStateException("Utility class");
    }

    private static final Logger LOG = Logger.getLogger(RetryUtil.class.getName());


    /**
     * Retry a given operation a number of times with an exponential backoff delay.
     * @param operation The operation to retry.
     * @param maxRetries The maximum number of retries.
     * @param initialDelayMs The initial delay in milliseconds.
     * @param operationName The name of the operation.
     * @param <T> The return type of the operation.
     * @return The result of the operation.
     */
    public static <T> T retry(Supplier<T> operation, int maxRetries, int initialDelayMs, String operationName) {
        int attempt = 0;
        int delay = initialDelayMs;
        while (attempt < maxRetries) {
            try {
                return operation.get();
            } catch (Exception e) {
                attempt++;
                LOG.info("OperationName: " + operationName + ", Attempt #: " + attempt);
                if (attempt >= maxRetries) {
                    throw e;
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RetryFailedException("Retry interrupted", ie);
                }
                delay = delay * 3;
            }
        }
        throw new RetryFailedException("Retry failed");
    }
}
