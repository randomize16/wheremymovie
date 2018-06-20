package org.bots.services;

import lombok.AllArgsConstructor;
import org.bots.clients.BotPlatform;
import org.bots.clients.BotPlatformType;
import org.bots.model.datebase.Client;
import org.bots.model.datebase.Movie;
import org.bots.model.datebase.MovieFile;
import org.bots.model.datebase.Subscription;
import org.bots.repository.MovieRepository;
import org.bots.repository.SubscriptionRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class SubscriptionCron {

    private final SubscriptionRepository subscriptionRepository;
    private final MovieRepository movieRepository;
    private final SearchService searchService;
    private final List<BotPlatform> botPlatforms;


    @Scheduled(cron = "0 0 10,17 * * *")
    void getUpdates(){
        List<Subscription> result = subscriptionRepository.findAll();
        if(!result.isEmpty()){
            result.forEach(this::processSubscriptionUpdates);
        }
    }

    @Async
    void processSubscriptionUpdates(Subscription subscription){
        Integer movieId = subscription.getMovieId();
        Movie oldMovie = movieRepository.findById(movieId);
        Movie newMovie = searchService.getAndSaveMovie(movieId);
        List<String> newFilesList = compareMovieFiles(oldMovie.getMovieFiles(), newMovie.getMovieFiles());
        if(!newFilesList.isEmpty()){
            String updateMessageText = "Updated files:\n" + newFilesList.stream().reduce((s, s2) -> s + "\n" + s2).orElse("");
            sendUpdateMessageToUser(updateMessageText, subscription.getSubscribers());
        }
    }

    private void sendUpdateMessageToUser(String text, List<Client> subscribers){
        Map<BotPlatformType, List<Client>> groupedByType = subscribers.stream().collect(Collectors.groupingBy(Client::getType, Collectors.toList()));
        botPlatforms.forEach(botPlatform -> {
            List<Client> clients = groupedByType.get(botPlatform.getType());
            clients.forEach(client -> botPlatform.sendSubsriptionMessage(client, text));
        });
    }

    private List<String> compareMovieFiles(List<MovieFile> oldFiles, List<MovieFile> newFiles){
        List<String> result = new ArrayList<>();
        Function<MovieFile, String> checkFileString = movieFile ->
                movieFile.getSource() + "#"
                        + movieFile.getSeries() + "#"
                        + movieFile.getTranslation() + "#"
                        + movieFile.getSeries();

        Set<String> oldFileList = oldFiles.stream().map(checkFileString)
                .collect(Collectors.toSet());
        newFiles.forEach(movieFile -> {
            String checkFile = checkFileString.apply(movieFile);
            if(!oldFileList.contains(checkFile)){
                StringBuilder str = new StringBuilder();
                if(movieFile.getSeason() != null)
                    str.append("Season ").append(movieFile.getSeason()).append(", ");
                if(movieFile.getTranslation() != null)
                    str.append("Translation ").append(movieFile.getTranslation()).append(", ");
                if(movieFile.getSeries() != null)
                    str.append("Series ").append(movieFile.getSeries()).append(", ");
                str.append(" on source ").append(movieFile.getSource());
                result.add(str.toString());
            }
        });
        return result;
    }

}
