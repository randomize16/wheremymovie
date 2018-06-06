package org.bots.model.items;

import java.util.Map;

public class MovieFileHierarchy {

    private String name;
    private String url;
    private FileHierarchyType type;
    private Integer order;

    private Map<Integer, MovieFileHierarchy> children;

    public enum FileHierarchyType {
        SOURCE, FOLDER, FILE, LINK
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public FileHierarchyType getType() {
        return type;
    }

    public void setType(FileHierarchyType type) {
        this.type = type;
    }

    public Map<Integer, MovieFileHierarchy> getChildren() {
        return children;
    }

    public void setChildren(Map<Integer, MovieFileHierarchy> children) {
        this.children = children;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    private MovieFileHierarchy() {}

    public static MovieFileHierarchy createSource(String name){
        MovieFileHierarchy movieFileHierarchy = new MovieFileHierarchy();
        movieFileHierarchy.setName(name);
        movieFileHierarchy.setType(FileHierarchyType.SOURCE);
        return movieFileHierarchy;
    }

    public static MovieFileHierarchy createFolder(String name){
        MovieFileHierarchy movieFileHierarchy = new MovieFileHierarchy();
        movieFileHierarchy.setName(name);
        movieFileHierarchy.setType(FileHierarchyType.FOLDER);
        return movieFileHierarchy;
    }

    public static MovieFileHierarchy createFile(String name, String url){
        MovieFileHierarchy movieFileHierarchy = new MovieFileHierarchy();
        movieFileHierarchy.setName(name);
        movieFileHierarchy.setType(FileHierarchyType.FILE);
        movieFileHierarchy.setUrl(url);
        return movieFileHierarchy;
    }

    public static MovieFileHierarchy createLink(String name, String url){
        MovieFileHierarchy movieFileHierarchy = new MovieFileHierarchy();
        movieFileHierarchy.setName(name);
        movieFileHierarchy.setType(FileHierarchyType.LINK);
        movieFileHierarchy.setUrl(url);
        return movieFileHierarchy;
    }

}
