package org.bots.model.datebase;

import org.bots.model.items.MovieContext;
import org.bots.model.items.Person;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Document(collection = "movie")
public class Movie {

    @Id
    private Integer id;
    private String title;
    private String originalTitle;
    private List<Person> directors;
    private List<Person> writers;
    private List<Person> casts;
    private Integer ratio;
    private LocalDate releaseDate;
    private FilmTypes filmTypes;
    private String status;
    private LocalDate updated;
    private List<String> location;
    private Integer duration;
    private String description;
    private String poster;
    private List<String> categories;

    private Map<String, String> movieInSource;


    private Map<String, MovieContext> movieContentLinks;

    public enum FilmTypes {
        FILM,
        SERIAL,
        MULT,
        MULTSERIAL;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public List<Person> getDirectors() {
        return directors;
    }

    public void setDirectors(List<Person> directors) {
        this.directors = directors;
    }

    public List<Person> getWriters() {
        return writers;
    }

    public void setWriters(List<Person> writers) {
        this.writers = writers;
    }

    public List<Person> getCasts() {
        return casts;
    }

    public void setCasts(List<Person> casts) {
        this.casts = casts;
    }

    public Integer getRatio() {
        return ratio;
    }

    public void setRatio(Integer ratio) {
        this.ratio = ratio;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public FilmTypes getFilmTypes() {
        return filmTypes;
    }

    public void setFilmTypes(FilmTypes filmTypes) {
        this.filmTypes = filmTypes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getUpdated() {
        return updated;
    }

    public void setUpdated(LocalDate updated) {
        this.updated = updated;
    }

    public List<String> getLocation() {
        return location;
    }

    public void setLocation(List<String> location) {
        this.location = location;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public Map<String, String> getMovieInSource() {
        return movieInSource;
    }

    public void setMovieInSource(Map<String, String> movieInSource) {
        this.movieInSource = movieInSource;
    }

    public Map<String, MovieContext> getMovieContentLinks() {
        return movieContentLinks;
    }

    public void setMovieContentLinks(Map<String, MovieContext> movieContentLinks) {
        this.movieContentLinks = movieContentLinks;
    }
}
