package org.bots.model.datebase;

import org.bots.clients.BotPlatformType;
import org.springframework.data.annotation.Id;

public class TelegramClient implements Client {

    @Id
    private final Integer clientId;
    private final Long chatId;

    private TelegramClient(Integer clientId, Long chatId) {
        this.clientId = clientId;
        this.chatId = chatId;
    }

    public static TelegramClient of(Integer id, Long chatId){
        return new TelegramClient(id,chatId);
    }

    @Override
    public BotPlatformType getType() {
        return BotPlatformType.TELEGRAM;
    }

    @Override
    public Integer getClientId() {
        return this.clientId;
    }

    @Override
    public Long getChatId() {
        return this.chatId;
    }
}
