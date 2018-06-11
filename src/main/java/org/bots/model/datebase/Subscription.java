package org.bots.model.datebase;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;

@Data
public class Subscription {

    @Id
    private Integer movieId;
    private List<Client> subscribers = new ArrayList<>();

}
