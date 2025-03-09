package com.jonathanfoucher.httpexample.connectors;

import com.jonathanfoucher.httpexample.connectors.interceptors.CorrelationIdInterceptor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Optional;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.util.CollectionUtils.isEmpty;

public abstract class Connector {
    private final RestTemplate restTemplate;

    protected Connector(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.additionalInterceptors(new CorrelationIdInterceptor())
                .build();
    }

    protected <T> Optional<T> get(String url, Map<String, String> customHeaders, Class<T> clazz, Map<String, String> pathParameters) {
        return call(GET, url, customHeaders, clazz, null, pathParameters, null);
    }

    protected <T, K> Optional<T> post(String url, Map<String, String> customHeaders, Class<T> clazz, K body) {
        return call(POST, url, customHeaders, clazz, null, null, body);
    }

    private <T, K> Optional<T> call(HttpMethod httpMethod, String url, Map<String, String> customHeaders, Class<T> clazz, Map<String, String> queryParameters, Map<String, String> pathParameters, K body) {
        HttpHeaders headers = getHeaders(customHeaders);
        HttpEntity<K> httpEntity = new HttpEntity<>(body, headers);
        return Optional.ofNullable(connect(url, httpEntity, httpMethod, clazz, queryParameters, pathParameters).getBody());
    }

    private HttpHeaders getHeaders(Map<String, String> headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        if (!isEmpty(headers)) {
            headers.forEach(httpHeaders::add);
        }
        return httpHeaders;
    }

    private <T, K> ResponseEntity<T> connect(String url, HttpEntity<K> httpEntity, HttpMethod method, Class<T> clazz, Map<String, String> queryParameters, Map<String, String> pathParameters) {
        String builtUrl;
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        if (!isEmpty(queryParameters)) {
            queryParameters.forEach(builder::replaceQueryParam);
        }

        if (!isEmpty(pathParameters)) {
            builtUrl = builder.buildAndExpand(pathParameters).toUriString();
        } else {
            builtUrl = builder.build().toUriString();
        }

        return restTemplate.exchange(builtUrl, method, httpEntity, clazz);
    }
}
