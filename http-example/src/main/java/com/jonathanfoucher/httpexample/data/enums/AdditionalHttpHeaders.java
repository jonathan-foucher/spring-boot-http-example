package com.jonathanfoucher.httpexample.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum AdditionalHttpHeaders {
    CORRELATION_ID_HEADER("x-correlation-id"),
    API_KEY_HEADER("x-api-key");

    private final String headerName;
}
