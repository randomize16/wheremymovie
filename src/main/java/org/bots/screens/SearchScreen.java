package org.bots.screens;

import org.bots.model.items.MovieSearchResponse;

import java.util.List;

public class SearchScreen{

    List<MovieSearchResponse> movieSearchResponses;
    List<String> buttons;

    public void setMovieSearchResponses(List<MovieSearchResponse> movieSearchResponses) {
        this.movieSearchResponses = movieSearchResponses;
    }

    public void setButtons(List<String> buttons) {
        this.buttons = buttons;
    }

    public List<String> getButtons() {
        return this.buttons;
    }

    public List<MovieSearchResponse> getMovieList() {
        return movieSearchResponses;
    }


}
