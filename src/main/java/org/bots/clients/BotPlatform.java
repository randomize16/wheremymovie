package org.bots.clients;

import org.bots.model.datebase.Client;

public interface BotPlatform {
    BotPlatformType getType();
    void sendSubsriptionMessage(Client client, String text);
}
