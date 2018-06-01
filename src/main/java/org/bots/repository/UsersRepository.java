package org.bots.repository;

import org.bots.model.datebase.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UsersRepository extends MongoRepository<User, Long> {
    List<User> findByLastName(String lastName);
    User findById(Integer id);
}
