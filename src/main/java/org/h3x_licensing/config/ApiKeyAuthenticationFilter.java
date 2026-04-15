package org.h3x_licensing.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String VALIDATION_PATH = "/api/v1/licenses/validate";

    private final AppSecurityProperties securityProperties;
    private final Map<String, FixedWindowCounter> requestCounters = new ConcurrentHashMap<>();

    public ApiKeyAuthenticationFilter(AppSecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !(HttpMethod.POST.matches(request.getMethod()) && VALIDATION_PATH.equals(request.getRequestURI()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String configuredApiKey = securityProperties.getApi().getValidationKey();
        if (configuredApiKey == null || configuredApiKey.isBlank()) {
            writeJsonError(response, HttpStatus.SERVICE_UNAVAILABLE, "API_VALIDATION_NOT_CONFIGURED", "The validation API key is not configured.");
            return;
        }

        String headerName = securityProperties.getApi().getValidationHeader();
        String providedApiKey = request.getHeader(headerName);
        if (providedApiKey == null || !configuredApiKey.equals(providedApiKey)) {
            writeJsonError(response, HttpStatus.UNAUTHORIZED, "INVALID_API_KEY", "The API key is missing or invalid.");
            return;
        }

        if (!withinRateLimit(request)) {
            writeJsonError(response, HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED", "Too many validation requests for this client.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean withinRateLimit(HttpServletRequest request) {
        int maxRequestsPerMinute = securityProperties.getApi().getMaxRequestsPerMinute();
        if (maxRequestsPerMinute <= 0) {
            return true;
        }

        String clientKey = request.getRemoteAddr();
        long nowEpochMilli = Instant.now().toEpochMilli();
        long windowStart = nowEpochMilli - (nowEpochMilli % 60_000);

        FixedWindowCounter counter = requestCounters.compute(clientKey, (key, existing) -> {
            if (existing == null || existing.windowStart != windowStart) {
                return new FixedWindowCounter(windowStart, 1);
            }
            existing.count++;
            return existing;
        });

        requestCounters.entrySet().removeIf(entry -> entry.getValue().windowStart < windowStart);
        return counter.count <= maxRequestsPerMinute;
    }

    private void writeJsonError(HttpServletResponse response, HttpStatus status, String code, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{" +
                "\"valid\":false," +
                "\"status\":\"" + code + "\"," +
                "\"message\":\"" + message + "\"" +
                "}");
    }

    private static final class FixedWindowCounter {
        private final long windowStart;
        private int count;

        private FixedWindowCounter(long windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}

