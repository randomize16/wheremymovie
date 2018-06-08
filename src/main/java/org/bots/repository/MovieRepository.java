package org.bots.repository;

import org.bots.model.datebase.Movie;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MovieRepository extends MongoRepository<Movie, Long> {
    Movie findById(Integer id);
    List<Movie> findAllByIdIn(List<Integer> id);

}
