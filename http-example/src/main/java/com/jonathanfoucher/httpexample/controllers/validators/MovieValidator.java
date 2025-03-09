package com.jonathanfoucher.httpexample.controllers.validators;

import com.jonathanfoucher.httpexample.common.errors.MovieNotValidException;
import com.jonathanfoucher.httpexample.data.dto.MovieDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.util.CollectionUtils.isEmpty;

@Component
public class MovieValidator {
    private static final int MAX_TITLE_LENGTH = 100;

    public void validateMovie(MovieDto movie) {
        List<String> errors = new ArrayList<>();

        if (movie.getId() == null) {
            errors.add("id field is required");
        }

        if (movie.getTitle() == null) {
            errors.add("title field is required");
        } else {
            checkTitleLength(movie.getTitle())
                    .ifPresent(errors::add);
        }

        if (movie.getReleaseDate() == null) {
            errors.add("release_date field is required");
        }

        if (!isEmpty(errors)) {
            throw new MovieNotValidException(errors);
        }
    }

    private Optional<String> checkTitleLength(String title) {
        if (title.length() > MAX_TITLE_LENGTH) {
            return Optional.of("title length should be equal or less than " + MAX_TITLE_LENGTH + " characters");
        }
        return Optional.empty();
    }
}
