package org.bots.model.datebase;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

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
    private List<MovieFile> movieFiles;

    public enum FilmTypes {
        FILM,
        SERIAL,
        MULT,
        MULTSERIAL;
    }

}
