package com.aidb.assistant.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Logs every HTTP request with method, URI, status, and duration.
 * Attaches a unique correlation ID to the MDC (Mapped Diagnostic Context)
 * so all log lines within a request share the same trace ID.
 */
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String MDC_CORRELATION_KEY = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        MDC.put(MDC_CORRELATION_KEY, correlationId);
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        long startTime = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            String queryString = request.getQueryString();
            String fullUri = queryString != null
                    ? request.getRequestURI() + "?" + queryString
                    : request.getRequestURI();

            log.info("[{}] {} {} -> {} ({}ms)",
                    correlationId,
                    request.getMethod(),
                    fullUri,
                    response.getStatus(),
                    duration);

            MDC.remove(MDC_CORRELATION_KEY);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Skip logging for actuator health check endpoints to reduce noise
        String uri = request.getRequestURI();
        return uri.startsWith("/actuator/health") || uri.startsWith("/actuator/info");
    }
}
