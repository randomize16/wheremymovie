package org.bots.model.datebase;

import org.springframework.data.annotation.Id;

public class Message {

    @Id
    private Integer id;
    private String text;
    private Command command;
}
