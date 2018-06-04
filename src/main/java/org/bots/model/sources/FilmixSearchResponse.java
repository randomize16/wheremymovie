package org.bots.model.sources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FilmixSearchResponse {

    private List<FilmixSearchMessage> message;
    private String type;
    private String field;

    public List<FilmixSearchMessage> getMessage() {
        return message;
    }

    public void setMessage(List<FilmixSearchMessage> message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }
}
