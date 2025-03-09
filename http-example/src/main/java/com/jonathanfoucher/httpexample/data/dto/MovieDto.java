package com.jonathanfoucher.httpexample.data.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MovieDto {
    private Long id;
    private String title;
    private LocalDate releaseDate;

    @Override
    public String toString() {
        return String.format("{ id=%s, title=\"%s\", release_date=%s }", id, title, releaseDate);
    }
}
