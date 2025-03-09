package com.jonathanfoucher.httpexample.controllers.validators;

import com.jonathanfoucher.httpexample.common.errors.MovieNotValidException;
import com.jonathanfoucher.httpexample.data.dto.MovieDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringJUnitConfig(MovieValidator.class)
class MovieValidatorTest {
    @Autowired
    private MovieValidator movieValidator;

    private static final int MAX_TITLE_LENGTH = 100;
    private static final Long ID = 15L;
    private static final String TITLE = "Some movie";
    private static final LocalDate RELEASE_DATE = LocalDate.of(2022, 7, 19);

    @Test
    void validateMovie() {
        // GIVEN
        MovieDto movie = initMovie();

        // WHEN / THEN
        movieValidator.validateMovie(movie);
    }

    @Test
    void validateMovieWithMissingId() {
        // GIVEN
        MovieDto movie = initMovie();
        movie.setId(null);

        // WHEN / THEN
        assertThatThrownBy(() -> movieValidator.validateMovie(movie))
                .isInstanceOf(MovieNotValidException.class)
                .hasMessage("Movie is not valid: \nid field is required");
    }

    @Test
    void validateMovieWithMissingTitle() {
        // GIVEN
        MovieDto movie = initMovie();
        movie.setTitle(null);

        // WHEN / THEN
        assertThatThrownBy(() -> movieValidator.validateMovie(movie))
                .isInstanceOf(MovieNotValidException.class)
                .hasMessage("Movie is not valid: \ntitle field is required");
    }

    @Test
    void validateMovieWithTitleTooLong() {
        // GIVEN
        MovieDto movie = initMovie();
        String tooLongTitle = "a".repeat(MAX_TITLE_LENGTH + 1);
        movie.setTitle(tooLongTitle);

        // WHEN / THEN
        assertThatThrownBy(() -> movieValidator.validateMovie(movie))
                .isInstanceOf(MovieNotValidException.class)
                .hasMessage("Movie is not valid: \ntitle length should be equal or less than 100 characters");
    }

    @Test
    void validateMovieWithMissingReleaseDate() {
        // GIVEN
        MovieDto movie = initMovie();
        movie.setReleaseDate(null);

        // WHEN / THEN
        assertThatThrownBy(() -> movieValidator.validateMovie(movie))
                .isInstanceOf(MovieNotValidException.class)
                .hasMessage("Movie is not valid: \nrelease_date field is required");
    }

    private MovieDto initMovie() {
        MovieDto movie = new MovieDto();
        movie.setId(ID);
        movie.setTitle(TITLE);
        movie.setReleaseDate(RELEASE_DATE);
        return movie;
    }
}
