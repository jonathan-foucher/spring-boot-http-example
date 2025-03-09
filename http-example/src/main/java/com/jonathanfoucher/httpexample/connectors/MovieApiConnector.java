package com.jonathanfoucher.httpexample.connectors;

import com.jonathanfoucher.httpexample.connectors.configs.MovieApiConfig;
import com.jonathanfoucher.httpexample.data.dto.MovieDto;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.jonathanfoucher.httpexample.data.enums.AdditionalHttpHeaders.API_KEY_HEADER;

@Component
public class MovieApiConnector extends Connector {
    private final MovieApiConfig movieApiConfig;

    public MovieApiConnector(RestTemplateBuilder restTemplateBuilder, MovieApiConfig movieApiConfig) {
        super(restTemplateBuilder);
        this.movieApiConfig = movieApiConfig;
    }

    public Optional<MovieDto> getMovieById(Long movieId) {
        String url = movieApiConfig.getBaseUrl() + movieApiConfig.getMovieByIdPath();

        Map<String, String> customHeaders = new HashMap<>();
        customHeaders.put(API_KEY_HEADER.getHeaderName(), movieApiConfig.getApiKey());

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("movie_id", String.valueOf(movieId));

        return get(url, customHeaders, MovieDto.class, pathParams);
    }

    public void saveMovie(MovieDto movie) {
        String url = movieApiConfig.getBaseUrl() + movieApiConfig.getMoviesPath();

        Map<String, String> customHeaders = new HashMap<>();
        customHeaders.put(API_KEY_HEADER.getHeaderName(), movieApiConfig.getApiKey());

        post(url, customHeaders, Void.class, movie);
    }
}
