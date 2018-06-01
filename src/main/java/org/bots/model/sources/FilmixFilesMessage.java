package org.bots.model.sources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FilmixFilesMessage {

    private Translation translations;

    public class Translation {
        @JsonProperty("pl")
        private String playlist;
        private Map<String, String> translatin;
    }

}
