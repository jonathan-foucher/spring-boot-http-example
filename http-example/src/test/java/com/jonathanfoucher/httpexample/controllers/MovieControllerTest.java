package com.jonathanfoucher.httpexample.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jonathanfoucher.httpexample.common.errors.MovieNotFoundException;
import com.jonathanfoucher.httpexample.common.errors.MovieNotValidException;
import com.jonathanfoucher.httpexample.common.filters.CorrelationIdFilter;
import com.jonathanfoucher.httpexample.controllers.advisers.CustomResponseEntityExceptionHandler;
import com.jonathanfoucher.httpexample.controllers.validators.MovieValidator;
import com.jonathanfoucher.httpexample.data.dto.MovieDto;
import com.jonathanfoucher.httpexample.services.MovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SNAKE_CASE;
import static com.jonathanfoucher.httpexample.data.enums.AdditionalHttpHeaders.CORRELATION_ID_HEADER;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.any;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MovieController.class)
@SpringJUnitConfig({MovieController.class, CustomResponseEntityExceptionHandler.class, CorrelationIdFilter.class})
class MovieControllerTest {
    private MockMvc mockMvc;
    @Autowired
    private MovieController movieController;
    @Autowired
    private CorrelationIdFilter correlationIdFilter;
    @Autowired
    private CustomResponseEntityExceptionHandler customResponseEntityExceptionHandler;
    @MockitoBean
    private MovieValidator movieValidator;
    @MockitoBean
    private MovieService movieService;

    private static final String MOVIE_BY_ID_PATH = "/movies/{id}";
    private static final String MOVIES_PATH = "/movies";

    private static final Long ID = 15L;
    private static final String TITLE = "Some movie";
    private static final LocalDate RELEASE_DATE = LocalDate.of(2022, 7, 19);

    private static final String CORRELATION_ID = "256a46fb-f91a-402a-b45e-065cbe5f2aa9";
    private static final Pattern CORRELATION_ID_REGEX_PATTERN = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    private static final Pattern TIMESTAMP_REGEX_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$");
    private static final String DEFAULT_TYPE = "about:blank";

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .propertyNamingStrategy(SNAKE_CASE)
                .build();
    }

    @BeforeEach
    void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(movieController)
                .setControllerAdvice(customResponseEntityExceptionHandler)
                .addFilter(correlationIdFilter)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void getMovieById() throws Exception {
        // GIVEN
        MovieDto movie = initMovie();

        when(movieService.getMovieById(ID))
                .thenReturn(movie);

        // WHEN / THEN
        mockMvc.perform(get(MOVIE_BY_ID_PATH, ID))
                .andExpect(status().isOk())
                .andExpect(header().exists(CORRELATION_ID_HEADER.getHeaderName()))
                .andExpect(header().string(CORRELATION_ID_HEADER.getHeaderName(), matchesPattern(CORRELATION_ID_REGEX_PATTERN)))
                .andExpect(content().string(objectMapper.writeValueAsString(movie)));

        verify(movieService, times(1)).getMovieById(ID);
    }

    @Test
    void getMovieByIdWithProvidedCorrelationIdHeader() throws Exception {
        // GIVEN
        MovieDto movie = initMovie();

        when(movieService.getMovieById(ID))
                .thenReturn(movie);

        // WHEN / THEN
        mockMvc.perform(get(MOVIE_BY_ID_PATH, ID).header(CORRELATION_ID_HEADER.getHeaderName(), CORRELATION_ID))
                .andExpect(status().isOk())
                .andExpect(header().exists(CORRELATION_ID_HEADER.getHeaderName()))
                .andExpect(header().string(CORRELATION_ID_HEADER.getHeaderName(), equalTo(CORRELATION_ID)))
                .andExpect(content().string(objectMapper.writeValueAsString(movie)));

        verify(movieService, times(1)).getMovieById(ID);
    }

    @Test
    void getMovieByIdWithMovieNotFound() throws Exception {
        // GIVEN
        when(movieService.getMovieById(ID))
                .thenThrow(new MovieNotFoundException(ID));

        // WHEN / THEN
        mockMvc.perform(get(MOVIE_BY_ID_PATH, ID))
                .andExpect(status().isNotFound())
                .andExpect(header().exists(CORRELATION_ID_HEADER.getHeaderName()))
                .andExpect(header().string(CORRELATION_ID_HEADER.getHeaderName(), matchesPattern(CORRELATION_ID_REGEX_PATTERN)))
                .andExpect(jsonPath("$.type", equalTo(DEFAULT_TYPE)))
                .andExpect(jsonPath("$.title", equalTo(NOT_FOUND.getReasonPhrase())))
                .andExpect(jsonPath("$.status", equalTo(NOT_FOUND.value())))
                .andExpect(jsonPath("$.detail", equalTo("Movie with id 15 is not found")))
                .andExpect(jsonPath("$.instance", equalTo("uri=/movies/15")))
                .andExpect(jsonPath("$.properties").isNotEmpty())
                .andExpect(jsonPath("$.properties.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.properties.timestamp", matchesPattern(TIMESTAMP_REGEX_PATTERN)));

        verify(movieService, times(1)).getMovieById(ID);
    }

    @Test
    void getMovieByIdWithInternalServerError() throws Exception {
        // GIVEN
        when(movieService.getMovieById(ID))
                .thenThrow(new RuntimeException("some error"));

        // WHEN / THEN
        mockMvc.perform(get(MOVIE_BY_ID_PATH, ID))
                .andExpect(status().isInternalServerError())
                .andExpect(header().exists(CORRELATION_ID_HEADER.getHeaderName()))
                .andExpect(header().string(CORRELATION_ID_HEADER.getHeaderName(), matchesPattern(CORRELATION_ID_REGEX_PATTERN)))
                .andExpect(jsonPath("$.type", equalTo(DEFAULT_TYPE)))
                .andExpect(jsonPath("$.title", equalTo(INTERNAL_SERVER_ERROR.getReasonPhrase())))
                .andExpect(jsonPath("$.status", equalTo(INTERNAL_SERVER_ERROR.value())))
                .andExpect(jsonPath("$.detail", equalTo("some error")))
                .andExpect(jsonPath("$.instance", equalTo("uri=/movies/15")))
                .andExpect(jsonPath("$.properties").isNotEmpty())
                .andExpect(jsonPath("$.properties.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.properties.timestamp", matchesPattern(TIMESTAMP_REGEX_PATTERN)));

        verify(movieService, times(1)).getMovieById(ID);
    }

    @Test
    void saveMovie() throws Exception {
        // GIVEN
        MovieDto movie = initMovie();

        // WHEN / THEN
        mockMvc.perform(post(MOVIES_PATH).contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movie)))
                .andExpect(header().exists(CORRELATION_ID_HEADER.getHeaderName()))
                .andExpect(header().string(CORRELATION_ID_HEADER.getHeaderName(), matchesPattern(CORRELATION_ID_REGEX_PATTERN)))
                .andExpect(status().isOk())
                .andExpect(content().string(emptyString()));

        ArgumentCaptor<MovieDto> capturedMovie = ArgumentCaptor.forClass(MovieDto.class);
        InOrder inOrder = inOrder(movieValidator, movieService);
        inOrder.verify(movieValidator, times(1)).validateMovie(capturedMovie.capture());
        inOrder.verify(movieService, times(1)).saveMovie(capturedMovie.capture());

        MovieDto validatedMovie = capturedMovie.getAllValues().get(0);
        checkMovie(validatedMovie);

        MovieDto savedMovie = capturedMovie.getAllValues().get(1);
        checkMovie(savedMovie);
    }

    @Test
    void saveMovieWithMovieNotValid() throws Exception {
        // GIVEN
        MovieDto movie = initMovie();
        movie.setTitle(null);

        doThrow(new MovieNotValidException(List.of("title field is required")))
                .when(movieValidator).validateMovie(any());

        // WHEN / THEN
        mockMvc.perform(post(MOVIES_PATH).contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movie)))
                .andExpect(header().exists(CORRELATION_ID_HEADER.getHeaderName()))
                .andExpect(header().string(CORRELATION_ID_HEADER.getHeaderName(), matchesPattern(CORRELATION_ID_REGEX_PATTERN)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type", equalTo(DEFAULT_TYPE)))
                .andExpect(jsonPath("$.title", equalTo(BAD_REQUEST.getReasonPhrase())))
                .andExpect(jsonPath("$.status", equalTo(BAD_REQUEST.value())))
                .andExpect(jsonPath("$.detail", equalTo("Movie is not valid: \ntitle field is required")))
                .andExpect(jsonPath("$.instance", equalTo("uri=/movies")))
                .andExpect(jsonPath("$.properties").isNotEmpty())
                .andExpect(jsonPath("$.properties.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.properties.timestamp", matchesPattern(TIMESTAMP_REGEX_PATTERN)));

        ArgumentCaptor<MovieDto> capturedMovie = ArgumentCaptor.forClass(MovieDto.class);
        verify(movieValidator, times(1)).validateMovie(capturedMovie.capture());
        verify(movieService, never()).saveMovie(any());

        MovieDto validatedMovie = capturedMovie.getAllValues().getFirst();
        assertNotNull(validatedMovie);
        assertEquals(ID, validatedMovie.getId());
        assertNull(validatedMovie.getTitle());
        assertEquals(RELEASE_DATE, validatedMovie.getReleaseDate());
    }

    private MovieDto initMovie() {
        MovieDto movie = new MovieDto();
        movie.setId(ID);
        movie.setTitle(TITLE);
        movie.setReleaseDate(RELEASE_DATE);
        return movie;
    }

    private void checkMovie(MovieDto movie) {
        assertNotNull(movie);
        assertEquals(ID, movie.getId());
        assertEquals(TITLE, movie.getTitle());
        assertEquals(RELEASE_DATE, movie.getReleaseDate());
    }
}
