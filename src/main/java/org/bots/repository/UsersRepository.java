package org.bots.repository;

import org.bots.model.datebase.Client;
import org.bots.model.datebase.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UsersRepository extends MongoRepository<User, Long> {

    User findByClients(Client client);
}
