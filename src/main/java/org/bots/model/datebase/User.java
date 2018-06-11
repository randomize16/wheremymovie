package org.bots.model.datebase;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "users")
@Data
public class User {

    @Id
    private Long id;
    private Long chatId;
    private String firstName;
    private String lastName;
    private String username;
    @Indexed
    private List<Client> clients;

    private List<Integer> favorites = new ArrayList<>();

    private String activeCommand;
}
