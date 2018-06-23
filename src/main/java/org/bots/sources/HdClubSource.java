package org.bots.sources;

import org.bots.model.datebase.Movie;
import org.bots.model.items.MovieSearchResponse;

import java.util.List;

public class HdClubSource implements MovieSources  {
    @Override
    public Movie getMovieById(Integer id) {
        return null;
    }

    @Override
    public List<MovieSearchResponse> searchMovie(String searchText) {
        return null;
    }
}
