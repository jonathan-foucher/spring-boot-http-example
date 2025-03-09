package com.jonathanfoucher.httpexample.common.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

import static com.jonathanfoucher.httpexample.data.enums.AdditionalHttpHeaders.CORRELATION_ID_HEADER;

@Component
@Order(1)
public class CorrelationIdFilter implements Filter {
    private static final Pattern CORRELATION_ID_REGEX_PATTERN = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    private static final String CORRELATION_ID_LOGS_VARIABLE = "correlation-id";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        String correlationId = request.getHeader(CORRELATION_ID_HEADER.getHeaderName());
        if (correlationId == null || !CORRELATION_ID_REGEX_PATTERN.matcher(correlationId).matches()) {
            correlationId = UUID.randomUUID().toString();
            request.setAttribute(CORRELATION_ID_HEADER.getHeaderName(), correlationId);
        }
        setCorrelationId(correlationId);

        HttpServletResponse response = (HttpServletResponse) servletResponse;
        response.setHeader(CORRELATION_ID_HEADER.getHeaderName(), correlationId);

        filterChain.doFilter(request, response);
        removeCorrelationId();
    }

    private void setCorrelationId(String correlationId) {
        MDC.put(CORRELATION_ID_LOGS_VARIABLE, correlationId);
    }

    private void removeCorrelationId() {
        MDC.remove(CORRELATION_ID_LOGS_VARIABLE);
    }
}
