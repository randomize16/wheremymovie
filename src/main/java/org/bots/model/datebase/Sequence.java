package org.bots.model.datebase;

import org.springframework.data.annotation.Id;

public class Sequence {
    @Id
    private String id;
    private Integer seq;

    public String getId() {
        return id;
    }

    public Integer getSeq() {
        return seq;
    }
}
