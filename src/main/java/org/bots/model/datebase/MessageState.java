package org.bots.model.datebase;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.List;
import java.util.Map;

@Data
public class MessageState {

    @Id
    private Long id;
    @Indexed
    private Long chatId;
    @Indexed
    private Integer messageId;
    private LinkType linkType = LinkType.URL;

    private Map<Integer, MovieFileHierarchy> movieFileHierarchies;
    private List<String> hierarchyPath;


    public enum LinkType{
        URL, VLC
    }

}
