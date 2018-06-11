package org.bots.model.datebase;

import lombok.Data;
import org.bots.model.items.MovieFileHierarchy;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
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
    private Boolean withPlaylist = false;
    private List<String> categories;
    private Map<String, List<MovieFile>> movieFiles;


    private Map<Integer, MovieFileHierarchy> movieFileHierarchy = new LinkedHashMap<>();

    public enum FilmTypes {
        FILM,
        SERIAL,
        MULT,
        MULTSERIAL;
    }

}
