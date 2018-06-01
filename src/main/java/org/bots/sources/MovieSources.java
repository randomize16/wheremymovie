package org.bots.sources;

import org.bots.model.datebase.Movie;
import org.bots.model.items.MovieSearchResponse;

import java.util.List;

public interface MovieSources {
    Movie getMovieById(Integer id);
    List<MovieSearchResponse> searchMovie(String searchText);
}
