package org.bots.repository;

import org.bots.model.datebase.Movie;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MovieRepository extends MongoRepository<Movie, Long> {
    Movie findById(Integer id);
}
