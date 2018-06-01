package org.bots.model.datebase;

import org.bots.clients.ClientTypes;
import org.springframework.data.annotation.Id;

public class TelegramClient implements Client {

    @Id
    Integer clientId;


    @Override
    public ClientTypes getType() {
        return ClientTypes.TELEGRAM;
    }

    @Override
    public Integer getClientId() {
        return this.clientId;
    }
}
