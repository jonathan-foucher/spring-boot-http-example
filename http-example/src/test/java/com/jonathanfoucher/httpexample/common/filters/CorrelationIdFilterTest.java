package com.jonathanfoucher.httpexample.common.filters;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

import static com.jonathanfoucher.httpexample.data.enums.AdditionalHttpHeaders.CORRELATION_ID_HEADER;
import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(CorrelationIdFilter.class)
class CorrelationIdFilterTest {
    @Autowired
    private CorrelationIdFilter correlationIdFilter;

    private static final Pattern CORRELATION_ID_REGEX_PATTERN = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");

    @Test
    void doFilterWithoutCorrelationId() throws ServletException, IOException {
        // GIVEN
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // WHEN
        correlationIdFilter.doFilter(request, response, chain);

        // THEN
        String resultCorrelationId = response.getHeader(CORRELATION_ID_HEADER.getHeaderName());
        assertNotNull(resultCorrelationId);
        assertTrue(CORRELATION_ID_REGEX_PATTERN.matcher(resultCorrelationId).matches());
    }

    @Test
    void doFilterWithNotValidCorrelationId() throws ServletException, IOException {
        // GIVEN
        String correlationId = "not-valid-correlation-id";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CORRELATION_ID_HEADER.getHeaderName(), correlationId);

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // WHEN
        correlationIdFilter.doFilter(request, response, chain);

        // THEN
        String resultCorrelationId = response.getHeader(CORRELATION_ID_HEADER.getHeaderName());
        assertNotNull(resultCorrelationId);
        assertTrue(CORRELATION_ID_REGEX_PATTERN.matcher(resultCorrelationId).matches());
    }

    @Test
    void doFilterWithValidCorrelationId() throws ServletException, IOException {
        // GIVEN
        String correlationId = UUID.randomUUID().toString();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CORRELATION_ID_HEADER.getHeaderName(), correlationId);

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // WHEN
        correlationIdFilter.doFilter(request, response, chain);

        // THEN
        String resultCorrelationId = response.getHeader(CORRELATION_ID_HEADER.getHeaderName());
        assertEquals(correlationId, resultCorrelationId);
    }
}
