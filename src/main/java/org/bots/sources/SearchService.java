package org.bots.sources;

import org.bots.model.datebase.Movie;
import org.bots.model.items.MovieSearchResponse;
import org.bots.screens.MovieScrean;
import org.bots.screens.SearchScreen;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {

    private final List<MovieSources> sources;
    private final FilmixSource filmixSource;

    public SearchService(List<MovieSources> sources, FilmixSource filmixSource) {
        this.sources = sources;
        this.filmixSource = filmixSource;
    }

    public SearchScreen searchMovie(String name){

        List<MovieSearchResponse> response = new ArrayList<>();
        sources.forEach(movieSources -> {
            response.addAll(movieSources.searchMovie(name));
        });
        SearchScreen screen = makeSearchScrean(response, 0);
        return screen;
    }

    public MovieScrean openMovie(Integer id){
        Movie movie = filmixSource.getMovieById(id);
        return makeMovieScrean(movie);
    }

    private void combineResponses() {
        //TODO: combine response from different sources
    }

    private SearchScreen makeSearchScrean(List<MovieSearchResponse> movieList, int offset) {
        SearchScreen response = new SearchScreen();
        response.setMovieSearchResponses(movieList);
        List<String> buttons = new ArrayList<>();
        movieList.forEach(movie -> buttons.add(movie.getId()));
        response.setButtons(buttons);
        return response;
    }

    private MovieScrean makeMovieScrean(Movie movie){
        MovieScrean movieScrean = new MovieScrean();
        movieScrean.setMovie(movie);
        return movieScrean;
    }

}
