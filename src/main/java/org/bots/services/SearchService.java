package org.bots.services;

import lombok.AllArgsConstructor;
import org.bots.model.datebase.Movie;
import org.bots.model.items.MovieSearchResponse;
import org.bots.repository.MovieRepository;
import org.bots.sources.FilmixSource;
import org.bots.sources.MovieSources;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class SearchService {

    private final List<MovieSources> sources;
    private final FilmixSource filmixSource;
    private final MovieRepository movieRepository;

    public List<MovieSearchResponse> searchMovie(String name){

        List<MovieSearchResponse> response = new ArrayList<>();
        sources.forEach(movieSources -> {
            response.addAll(movieSources.searchMovie(name));
        });
        return response;
    }

    public Movie getAndSaveMovie(Integer id){
        Movie movie = filmixSource.getMovieById(id);
        movieRepository.save(movie);
        return movie;
    }

    public List<Movie> getMovieByIdList(List<Integer> ids){
        List<Movie> movies = movieRepository.findAllByIdIn(ids);
        return movies;
    }

    private void combineResponses() {
        //TODO: combine response from different sources
    }


}
