package com.jonathanfoucher.httpexample.connectors.interceptors;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;

import static com.jonathanfoucher.httpexample.data.enums.AdditionalHttpHeaders.CORRELATION_ID_HEADER;

@Component
public class CorrelationIdInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String correlationId = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest()
                .getAttribute(CORRELATION_ID_HEADER.getHeaderName())
                .toString();

        request.getHeaders()
                .add(CORRELATION_ID_HEADER.getHeaderName(), correlationId);

        return execution.execute(request, body);
    }
}
