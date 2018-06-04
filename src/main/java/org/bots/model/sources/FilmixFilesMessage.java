package org.bots.model.sources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FilmixFilesMessage {

    private Translation translations;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Translation {
        @JsonProperty("pl")
        private String playlist;
        private Map<String, String> flash;

        public String getPlaylist() {
            return playlist;
        }

        public void setPlaylist(String playlist) {
            this.playlist = playlist;
        }

        public Map<String, String> getFlash() {
            return flash;
        }

        public void setFlash(Map<String, String> flash) {
            this.flash = flash;
        }
    }

    public Translation getTranslations() {
        return translations;
    }

    public void setTranslations(Translation translations) {
        this.translations = translations;
    }
}
