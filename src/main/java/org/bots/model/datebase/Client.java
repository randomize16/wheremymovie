package org.bots.model.datebase;

import org.bots.clients.ClientTypes;

public interface Client {
    ClientTypes getType();
    Integer getClientId();
}
