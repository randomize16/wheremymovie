package org.bots.services;

import lombok.AllArgsConstructor;
import org.bots.model.datebase.Client;
import org.bots.model.datebase.Subscription;
import org.bots.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

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

    public void unsubscribe(Client client, Integer movieId){
        Optional<Subscription> result = subscriptionRepository.findById(movieId);
        if(result.isPresent()){
            Subscription subscription = result.get();
            subscription.setSubscribers(subscription.getSubscribers().stream().filter(cl -> !(cl.getChatId().equals(client.getChatId())
                    && cl.getType().equals(client.getType())
                    && cl.getClientId().equals(client.getClientId())))
                    .collect(Collectors.toList()));
            if(subscription.getSubscribers().isEmpty())
                subscriptionRepository.delete(subscription);
            else
                subscriptionRepository.save(subscription);
        }
    }

    public boolean isSubscribed(Client client, Integer movieId){
        return subscriptionRepository.existsByMovieIdAndSubscribers(movieId, client);
    }

    public void changeSubscriptinState(Client client, Integer movieId){
       if(isSubscribed(client, movieId)){
           unsubscribe(client,movieId);
       }else{
           subscribe(client, movieId);
       }
    }

}
