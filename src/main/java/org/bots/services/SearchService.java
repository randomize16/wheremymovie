package org.bots.services;

import org.bots.model.datebase.Movie;
import org.bots.model.items.MovieSearchResponse;
import org.bots.repository.MovieRepository;
import org.bots.sources.FilmixSource;
import org.bots.sources.MovieSources;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {

    private final List<MovieSources> sources;
    private final FilmixSource filmixSource;
    private final MovieRepository movieRepository;

    public SearchService(List<MovieSources> sources, FilmixSource filmixSource, MovieRepository movieRepository) {
        this.sources = sources;
        this.filmixSource = filmixSource;
        this.movieRepository = movieRepository;
    }

    public List<MovieSearchResponse> searchMovie(String name){

        List<MovieSearchResponse> response = new ArrayList<>();
        sources.forEach(movieSources -> {
            response.addAll(movieSources.searchMovie(name));
        });
        return response;
    }

    public Movie getMovie(Integer id){
        Movie movie = filmixSource.getMovieById(id);
        movieRepository.save(movie);
        return movie;
    }

    private void combineResponses() {
        //TODO: combine response from different sources
    }


}
