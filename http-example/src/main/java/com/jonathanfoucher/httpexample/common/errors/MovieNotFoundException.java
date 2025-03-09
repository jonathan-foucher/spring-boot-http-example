package com.jonathanfoucher.httpexample.common.errors;

public class MovieNotFoundException extends RuntimeException {
    public MovieNotFoundException(Long movieId) {
        super("Movie with id " + movieId + " is not found");
    }
}
