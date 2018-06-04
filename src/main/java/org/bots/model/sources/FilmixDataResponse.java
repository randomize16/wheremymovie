package org.bots.model.sources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FilmixDataResponse {

    private FilmixFilesMessage message;
    private String type;
    private String field;

    public FilmixFilesMessage getMessage() {
        return message;
    }

    public void setMessage(FilmixFilesMessage message) {
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
