package com.imaginamos.farmatodo.backend.util;

import com.google.api.server.spi.response.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Clase utilitaria para gestionar instancias de CircuitBreaker y Retry
 * de manera centralizada, evitando la creación de múltiples instancias.
 */
public class ResilienceManager {
    // Circuit Breaker configuration constants
    private static final int CIRCUIT_BREAKER_FAILURE_RATE_THRESHOLD = 40;
    private static final int CIRCUIT_BREAKER_WAIT_DURATION_MS = 30000;
    private static final int CIRCUIT_BREAKER_PERMITTED_CALLS_HALF_OPEN = 2;
    private static final int CIRCUIT_BREAKER_SLIDING_WINDOW_SIZE = 20;

    // Retry configuration constants
    private static final int RETRY_MAX_ATTEMPTS = 2;
    private static final int RETRY_WAIT_DURATION_MS = 5000;
    private static final int RETRY_MAX_ATTEMPTS_CREATE_ORDER = 1; // 1 -> sin reintentos

    // Mapas para almacenar las instancias de CircuitBreaker y Retry
    private static final ConcurrentHashMap<String, CircuitBreaker> circuitBreakerMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Retry> retryMap = new ConcurrentHashMap<>();

    // Identificadores para los diferentes servicios
    public static final String SHOPPING_CART_SERVICE = "shoppingCart";
    public static final String CREATE_ORDER_SERVICE = "createOrder";

    /**
     * Obtiene una instancia de CircuitBreaker para el servicio especificado.
     * Si no existe, crea una nueva instancia.
     *
     * @param serviceName Identificador del servicio
     * @return Instancia de CircuitBreaker
     */
    public static CircuitBreaker getCircuitBreaker(String serviceName) {
        return circuitBreakerMap.computeIfAbsent(serviceName, key -> {
            CircuitBreakerConfig config = CircuitBreakerConfig.ofDefaults();

            // Configuraciones específicas para cada servicio
            if (CREATE_ORDER_SERVICE.equals(key) || SHOPPING_CART_SERVICE.equals(key)) {
                config = CircuitBreakerConfig.custom()
                        .failureRateThreshold(CIRCUIT_BREAKER_FAILURE_RATE_THRESHOLD)
                        .waitDurationInOpenState(Duration.ofMillis(CIRCUIT_BREAKER_WAIT_DURATION_MS))
                        .permittedNumberOfCallsInHalfOpenState(CIRCUIT_BREAKER_PERMITTED_CALLS_HALF_OPEN)
                        .slidingWindowSize(CIRCUIT_BREAKER_SLIDING_WINDOW_SIZE)
                        .recordExceptions(
                                RuntimeException.class,
                                CompletionException.class,
                                IOException.class,
                                ServiceUnavailableException.class
                        )
                        .build();
            }

            return CircuitBreaker.of(key + "CircuitBreaker", config);
        });
    }

    /**
     * Obtiene una instancia de Retry para el servicio especificado.
     * Si no existe, crea una nueva instancia.
     *
     * @param serviceName Identificador del servicio
     * @return Instancia de Retry
     */
    public static Retry getRetry(String serviceName) {
        return retryMap.computeIfAbsent(serviceName, key -> {
            RetryConfig config = RetryConfig.ofDefaults();

            if (CREATE_ORDER_SERVICE.equals(key)) {
                config = RetryConfig.custom()
                        .maxAttempts(RETRY_MAX_ATTEMPTS_CREATE_ORDER)
                        .retryExceptions(IOException.class, Exception.class)
                        .build();
            }
            // Mantener la configuración original para otros servicios
            else if (SHOPPING_CART_SERVICE.equals(key)) {
                config = RetryConfig.custom()
                        .maxAttempts(RETRY_MAX_ATTEMPTS)
                        .waitDuration(Duration.ofMillis(RETRY_WAIT_DURATION_MS))
                        .retryExceptions(IOException.class, Exception.class)
                        .build();
            }

            return Retry.of(key + "Retry", config);
        });
    }
}