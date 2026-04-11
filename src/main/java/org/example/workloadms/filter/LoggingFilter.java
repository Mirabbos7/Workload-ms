package org.example.workloadms.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(1)
public class LoggingFilter implements Filter {

    private static final String TRANSACTION_ID = "transactionId";

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String transactionId = httpRequest.getHeader("X-Transaction-Id");
        if (transactionId == null || transactionId.isBlank()) {
            transactionId = UUID.randomUUID().toString();
        }        MDC.put(TRANSACTION_ID, transactionId);

        httpResponse.setHeader("X-Transaction-Id", transactionId);
        String method = httpRequest.getMethod();
        String uri = httpRequest.getRequestURI();
        String query = httpRequest.getQueryString();
        String fullUrl = query != null ? uri + "?" + query : uri;

        log.info("[{}] --> {} {}", transactionId, method, fullUrl);

        long startTime = System.currentTimeMillis();

        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = httpResponse.getStatus();

            if (status >= 400) {
                log.error("[{}] <-- {} {} | status={} | {}ms",
                        transactionId, method, fullUrl, status, duration);
            } else {
                log.info("[{}] <-- {} {} | status={} | {}ms",
                        transactionId, method, fullUrl, status, duration);
            }

            MDC.remove(TRANSACTION_ID);
        }
    }
}