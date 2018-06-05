package org.bots.model.sources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FilmixPlaylist {
    private String comment;
    private List<FilmixPlaylist> playlist;
    private String serieId;
    private String season;
    private String file;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<FilmixPlaylist> getPlaylist() {
        return playlist;
    }

    public void setPlaylist(List<FilmixPlaylist> playlist) {
        this.playlist = playlist;
    }

    public String getSerieId() {
        return serieId;
    }

    public void setSerieId(String serieId) {
        this.serieId = serieId;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
}
