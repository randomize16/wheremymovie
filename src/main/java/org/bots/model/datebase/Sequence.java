package org.bots.model.datebase;

import org.springframework.data.annotation.Id;

public class Sequence {
    @Id
    private String id;
    private Long seq;

    public String getId() {
        return id;
    }

    public Long getSeq() {
        return seq;
    }
}
