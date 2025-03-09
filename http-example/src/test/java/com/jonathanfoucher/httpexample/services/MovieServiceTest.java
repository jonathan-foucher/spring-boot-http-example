package com.jonathanfoucher.httpexample.services;

import com.jonathanfoucher.httpexample.common.errors.MovieNotFoundException;
import com.jonathanfoucher.httpexample.connectors.MovieApiConnector;
import com.jonathanfoucher.httpexample.data.dto.MovieDto;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(MovieService.class)
class MovieServiceTest {
    @Autowired
    private MovieService movieService;
    @MockitoBean
    private MovieApiConnector movieApiConnector;

    private static final Long ID = 15L;
    private static final String TITLE = "Some movie";
    private static final LocalDate RELEASE_DATE = LocalDate.of(2022, 7, 19);

    @Test
    void getMovieById() {
        // GIVEN
        MovieDto movie = initMovie();

        when(movieApiConnector.getMovieById(ID))
                .thenReturn(Optional.of(movie));

        // WHEN
        MovieDto result = movieService.getMovieById(ID);

        // THEN
        verify(movieApiConnector, times(1)).getMovieById(ID);

        assertNotNull(result);
        assertEquals(ID, result.getId());
        assertEquals(TITLE, result.getTitle());
        assertEquals(RELEASE_DATE, result.getReleaseDate());
    }

    @Test
    void getMovieByIdWithoutResult() {
        // GIVEN
        when(movieApiConnector.getMovieById(ID))
                .thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> movieService.getMovieById(ID))
                .isInstanceOf(MovieNotFoundException.class)
                .hasMessage("Movie with id 15 is not found");

        verify(movieApiConnector, times(1)).getMovieById(ID);
    }

    @Test
    void saveMovie() {
        // GIVEN
        MovieDto movie = initMovie();

        // WHEN
        movieService.saveMovie(movie);

        // THEN
        ArgumentCaptor<MovieDto> capturedMovie = ArgumentCaptor.forClass(MovieDto.class);
        verify(movieApiConnector, times(1)).saveMovie(capturedMovie.capture());

        MovieDto result = capturedMovie.getValue();
        assertNotNull(result);
        assertEquals(ID, result.getId());
        assertEquals(TITLE, result.getTitle());
        assertEquals(RELEASE_DATE, result.getReleaseDate());
    }

    private MovieDto initMovie() {
        MovieDto movie = new MovieDto();
        movie.setId(ID);
        movie.setTitle(TITLE);
        movie.setReleaseDate(RELEASE_DATE);
        return movie;
    }
}
