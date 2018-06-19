package org.bots.model.datebase;

import org.bots.clients.BotPlatformType;

public interface Client {
    BotPlatformType getType();
    Integer getClientId();
    Long getChatId();
}
