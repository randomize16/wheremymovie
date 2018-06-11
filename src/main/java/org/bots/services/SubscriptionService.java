package org.bots.services;

import lombok.AllArgsConstructor;
import org.bots.model.datebase.Client;
import org.bots.model.datebase.Subscription;
import org.bots.repository.SubscriptionRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SearchService searchService;

    public void subscribe(Integer movieId, Client client){
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

    public void unsubscribe(Integer movieId, Client client){
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

    public boolean isSubscribed(Integer movieId, Client client){
        return subscriptionRepository.existsByMovieIdAndSubscribers(movieId, client);
    }

    @Scheduled(fixedRate = 5000)
    private void getUpdates(){
        List<Subscription> result = subscriptionRepository.findAll();
        if(!result.isEmpty()){
            result.forEach(this::processSubscriptionUpdates);
        }
    }

    @Async
    void processSubscriptionUpdates(Subscription subscription){
//        Integer movieId = subscription.getMovieId();
//        Movie movie = searchService.getAndSaveMovie(movieId);
        System.out.println("asdasd");
    }

}
