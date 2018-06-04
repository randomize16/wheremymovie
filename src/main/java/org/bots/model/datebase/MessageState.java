package org.bots.model.datebase;

import org.bots.model.items.MovieFileHierarchy;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.List;
import java.util.Map;

public class MessageState {

    @Id
    private Long id;
    @Indexed
    private Long chatId;
    @Indexed
    private Integer messageId;

    private Map<Integer, MovieFileHierarchy> movieFileHierarchy;
    private List<String> hierarhyPath;

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public Integer getMessageId() {
        return messageId;
    }

    public void setMessageId(Integer messageId) {
        this.messageId = messageId;
    }

    public Map<Integer, MovieFileHierarchy> getMovieFileHierarchy() {
        return movieFileHierarchy;
    }

    public void setMovieFileHierarchy(Map<Integer, MovieFileHierarchy> movieFileHierarchy) {
        this.movieFileHierarchy = movieFileHierarchy;
    }

    public List<String> getHierarhyPath() {
        return hierarhyPath;
    }

    public void setHierarhyPath(List<String> hierarhyPath) {
        this.hierarhyPath = hierarhyPath;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
