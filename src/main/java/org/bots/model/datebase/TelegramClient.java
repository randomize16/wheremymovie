package org.bots.model.datebase;

import org.bots.clients.ClientTypes;
import org.springframework.data.annotation.Id;

public class TelegramClient implements Client {

    @Id
    private Integer clientId;

    private TelegramClient(Integer clientId) {
        this.clientId = clientId;
    }

    public static TelegramClient of(Integer id){
        return new TelegramClient(id);
    }

    @Override
    public ClientTypes getType() {
        return ClientTypes.TELEGRAM;
    }

    @Override
    public Integer getClientId() {
        return this.clientId;
    }
}
