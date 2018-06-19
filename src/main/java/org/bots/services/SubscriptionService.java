package org.bots.services;

import lombok.AllArgsConstructor;
import org.bots.model.datebase.Client;
import org.bots.model.datebase.Subscription;
import org.bots.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    private void subscribe(Client client, Integer movieId){
        Subscription subscription;
        Optional<Subscription> result = subscriptionRepository.findById(movieId);
        if(result.isPresent()){
            subscription = result.get();
        }else{
            subscription = new Subscription();
            subscription.setMovieId(movieId);
            subscription.getSubscribers().add(client);
        }
        subscriptionRepository.save(subscription);
    }

    private void unsubscribe(Client client, Integer movieId){
        Optional<Subscription> result = subscriptionRepository.findById(movieId);
        if(result.isPresent()){
            Subscription subscription = result.get();
            subscription.getSubscribers().remove(client);
            if(subscription.getSubscribers().isEmpty())
                subscriptionRepository.delete(subscription);
            else
                subscriptionRepository.save(subscription);
        }
    }

    private boolean isSubscribed(Client client, Integer movieId){
        return subscriptionRepository.existsByMovieIdAndSubscribers(movieId, client);
    }

}
