package org.bots.model.datebase;

import org.bots.model.items.MovieFileHierarchy;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "movie")
public class Movie {

    @Id
    private Integer id;
    private String title;
    private String originalTitle;
    private List<String> directors;
    private List<String> writers;
    private List<String> casts;
    private String ratio;
    private String date;
    private FilmTypes filmTypes;
    private String status;
    private LocalDate updated;
    private List<String> location;
    private Integer duration;
    private String description;
    private String poster;
    private List<String> categories;


    private Map<Integer, MovieFileHierarchy> movieFileHierarchy = new LinkedHashMap<>();

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

    public List<String> getDirectors() {
        return directors;
    }

    public void setDirectors(List<String> directors) {
        this.directors = directors;
    }

    public List<String> getWriters() {
        return writers;
    }

    public void setWriters(List<String> writers) {
        this.writers = writers;
    }

    public List<String> getCasts() {
        return casts;
    }

    public void setCasts(List<String> casts) {
        this.casts = casts;
    }

    public String getRatio() {
        return ratio;
    }

    public void setRatio(String ratio) {
        this.ratio = ratio;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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

    public Map<Integer, MovieFileHierarchy> getMovieFileHierarchy() {
        return movieFileHierarchy;
    }

    public void setMovieFileHierarchy(Map<Integer, MovieFileHierarchy> movieFileHierarchy) {
        this.movieFileHierarchy = movieFileHierarchy;
    }
}