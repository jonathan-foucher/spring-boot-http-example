package com.jonathanfoucher.httpexample.common.errors;

import org.apache.tomcat.util.buf.StringUtils;

import java.util.List;

public class MovieNotValidException extends RuntimeException {
    public MovieNotValidException(List<String> errors) {
        super("Movie is not valid: \n" + StringUtils.join(errors, '\n'));
    }
}
