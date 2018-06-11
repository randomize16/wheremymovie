package org.bots.repository;

import org.bots.model.datebase.Client;
import org.bots.model.datebase.Subscription;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SubscriptionRepository extends MongoRepository<Subscription, Integer> {

    boolean existsByMovieIdAndSubscribers(Integer movieId, Client client);

}
