package com.jonathanfoucher.httpexample.services;

import com.jonathanfoucher.httpexample.common.errors.MovieNotFoundException;
import com.jonathanfoucher.httpexample.connectors.MovieApiConnector;
import com.jonathanfoucher.httpexample.data.dto.MovieDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieService {
    private final MovieApiConnector movieApiConnector;

    public MovieDto getMovieById(Long movieId) {
        return movieApiConnector.getMovieById(movieId)
                .orElseThrow(() -> new MovieNotFoundException(movieId));
    }

    public void saveMovie(MovieDto movie) {
        movieApiConnector.saveMovie(movie);
    }

    @Scheduled(cron = "0 */5 * * * *")
    public void cronExample() {
        log.info("Cron example has been called");
    }
}
