package com.jonathanfoucher.httpexample.connectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jonathanfoucher.httpexample.connectors.configs.MovieApiConfig;
import com.jonathanfoucher.httpexample.data.dto.MovieDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.Optional;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SNAKE_CASE;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static com.jonathanfoucher.httpexample.data.enums.AdditionalHttpHeaders.API_KEY_HEADER;
import static com.jonathanfoucher.httpexample.data.enums.AdditionalHttpHeaders.CORRELATION_ID_HEADER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringJUnitConfig(MovieApiConnector.class)
@RestClientTest(MovieApiConnector.class)
@ActiveProfiles("test")
class MovieApiConnectorTest {
    @Autowired
    private MovieApiConnector movieApiConnector;
    @MockitoBean
    private MovieApiConfig movieApiConfig;

    private MockRestServiceServer server;

    private static final String BASE_URL = "http://localhost:8091/movie-api";
    private static final String API_KEY = "some-api-key";
    private static final String MOVIES_PATH = "/movies";
    private static final String MOVIE_BY_ID_PATH = "/movies/{movie_id}";
    private static final String CORRELATION_ID = "256a46fb-f91a-402a-b45e-065cbe5f2aa9";

    private static final Long ID = 15L;
    private static final String TITLE = "Some movie";
    private static final LocalDate RELEASE_DATE = LocalDate.of(2022, 7, 19);

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .propertyNamingStrategy(SNAKE_CASE)
                .configure(WRITE_DATES_AS_TIMESTAMPS, false)
                .build();
    }

    @BeforeEach
    void init() {
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(movieApiConnector, "restTemplate");
        assertNotNull(restTemplate);
        server = MockRestServiceServer.createServer(restTemplate);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(CORRELATION_ID_HEADER.getHeaderName(), CORRELATION_ID);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        when(movieApiConfig.getBaseUrl())
                .thenReturn(BASE_URL);
        when(movieApiConfig.getApiKey())
                .thenReturn(API_KEY);
        when(movieApiConfig.getMoviesPath())
                .thenReturn(MOVIES_PATH);
        when(movieApiConfig.getMovieByIdPath())
                .thenReturn(MOVIE_BY_ID_PATH);
    }

    private URI getMovieByIdUri() {
        return UriComponentsBuilder.fromUriString(BASE_URL)
                .path(MOVIE_BY_ID_PATH)
                .buildAndExpand(ID)
                .toUri();
    }

    private URI getMoviesUri() {
        return UriComponentsBuilder.fromUriString(BASE_URL)
                .path(MOVIES_PATH)
                .build()
                .toUri();
    }

    @Test
    void getMovieById() throws JsonProcessingException {
        // GIVEN
        MovieDto movie = initMovie();

        server.expect(once(), requestTo(getMovieByIdUri()))
                .andExpect(method(GET))
                .andExpect(header(CORRELATION_ID_HEADER.getHeaderName(), CORRELATION_ID))
                .andExpect(header(API_KEY_HEADER.getHeaderName(), API_KEY))
                .andRespond(withSuccess(objectMapper.writeValueAsString(movie), APPLICATION_JSON));

        // WHEN
        Optional<MovieDto> resultOpt = movieApiConnector.getMovieById(ID);

        // THEN
        assertNotNull(resultOpt);
        assertTrue(resultOpt.isPresent());

        MovieDto result = resultOpt.get();
        assertNotNull(result);
        assertEquals(ID, result.getId());
        assertEquals(TITLE, result.getTitle());
        assertEquals(RELEASE_DATE, result.getReleaseDate());
    }

    @Test
    void getMovieByIdWithoutResult() throws JsonProcessingException {
        // GIVEN
        server.expect(once(), requestTo(getMovieByIdUri()))
                .andExpect(method(GET))
                .andExpect(header(CORRELATION_ID_HEADER.getHeaderName(), CORRELATION_ID))
                .andExpect(header(API_KEY_HEADER.getHeaderName(), API_KEY))
                .andRespond(withSuccess(objectMapper.writeValueAsString(null), APPLICATION_JSON));

        // WHEN
        Optional<MovieDto> resultOpt = movieApiConnector.getMovieById(ID);

        // THEN
        assertNotNull(resultOpt);
        assertTrue(resultOpt.isEmpty());
    }

    @Test
    void saveMovie() throws JsonProcessingException {
        // GIVEN
        MovieDto movie = initMovie();

        server.expect(once(), requestTo(getMoviesUri()))
                .andExpect(method(POST))
                .andExpect(header(CORRELATION_ID_HEADER.getHeaderName(), CORRELATION_ID))
                .andExpect(header(API_KEY_HEADER.getHeaderName(), API_KEY))
                .andExpect(header(CONTENT_TYPE, APPLICATION_JSON_VALUE))
                .andExpect(content().string(objectMapper.writeValueAsString(movie)))
                .andRespond(withSuccess());

        // WHEN
        movieApiConnector.saveMovie(movie);
    }

    private MovieDto initMovie() {
        MovieDto movie = new MovieDto();
        movie.setId(ID);
        movie.setTitle(TITLE);
        movie.setReleaseDate(RELEASE_DATE);
        return movie;
    }
}
