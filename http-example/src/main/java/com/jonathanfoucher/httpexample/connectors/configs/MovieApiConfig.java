package com.jonathanfoucher.httpexample.connectors.configs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("movie-api")
public class MovieApiConfig {
    private String baseUrl;
    private String apiKey;
    private String moviesPath;
    private String movieByIdPath;
}
