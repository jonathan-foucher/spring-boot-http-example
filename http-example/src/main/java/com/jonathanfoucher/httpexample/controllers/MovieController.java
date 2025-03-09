package com.jonathanfoucher.httpexample.controllers;

import com.jonathanfoucher.httpexample.controllers.validators.MovieValidator;
import com.jonathanfoucher.httpexample.data.dto.MovieDto;
import com.jonathanfoucher.httpexample.services.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
public class MovieController {
    private final MovieValidator movieValidator;
    private final MovieService movieService;

    @GetMapping("/{movie_id}")
    public MovieDto getMovie(@PathVariable("movie_id") Long movieId) {
        return movieService.getMovieById(movieId);
    }

    @PostMapping
    public void saveMovie(@RequestBody MovieDto movie) {
        movieValidator.validateMovie(movie);
        movieService.saveMovie(movie);
    }
}
