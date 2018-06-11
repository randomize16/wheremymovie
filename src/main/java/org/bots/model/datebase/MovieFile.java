package org.bots.model.datebase;

import lombok.Data;

@Data
public class MovieFile {
    private String name;
    private Integer series;
    private Integer season;
    private String translation;
    private String source;
    private String url;
}
