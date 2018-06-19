package org.bots.model.datebase;

import lombok.Data;

import java.util.Map;

@Data
public class MovieFileHierarchy {

    private String name;
    private String url;
    private FileHierarchyType type;
    private Integer order;
    private Integer level;

    private Map<Integer, MovieFileHierarchy> children;

    public enum FileHierarchyType {
        TRANSLATION, SEASON, FILE, LINK
    }

    private MovieFileHierarchy() {}

    public static MovieFileHierarchy createSeason(String name){
        MovieFileHierarchy movieFileHierarchy = new MovieFileHierarchy();
        movieFileHierarchy.setName(name);
        movieFileHierarchy.setLevel(1);
        movieFileHierarchy.setType(FileHierarchyType.SEASON);
        return movieFileHierarchy;
    }

    public static MovieFileHierarchy createTranslation(String name){
        MovieFileHierarchy movieFileHierarchy = new MovieFileHierarchy();
        movieFileHierarchy.setName(name);
        movieFileHierarchy.setLevel(2);
        movieFileHierarchy.setType(FileHierarchyType.TRANSLATION);
        return movieFileHierarchy;
    }

    public static MovieFileHierarchy createSeries(String name){
        MovieFileHierarchy movieFileHierarchy = new MovieFileHierarchy();
        movieFileHierarchy.setName(name);
        movieFileHierarchy.setLevel(3);
        movieFileHierarchy.setType(FileHierarchyType.FILE);
        return movieFileHierarchy;
    }

    public static MovieFileHierarchy createLink(String name, String url){
        MovieFileHierarchy movieFileHierarchy = new MovieFileHierarchy();
        movieFileHierarchy.setName(name);
        movieFileHierarchy.setLevel(4);
        movieFileHierarchy.setType(FileHierarchyType.LINK);
        movieFileHierarchy.setUrl(url);
        return movieFileHierarchy;
    }

}
