package org.bots.core;

import org.bots.model.datebase.MessageState;
import org.bots.model.datebase.Movie;
import org.bots.model.items.Button;
import org.bots.model.items.MovieFileHierarchy;
import org.bots.repository.MessageStateRepository;
import org.bots.repository.MovieRepository;
import org.bots.sources.SearchService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MessageStateService {

    private final MovieRepository movieRepository;
    private final SearchService searchService;
    private final MessageStateRepository messageStateRepository;
    private final NextSequenceService sequanceService;


    public MessageStateService(MovieRepository movieRepository, SearchService searchService, MessageStateRepository messageStateRepository, NextSequenceService sequanceService) {
        this.movieRepository = movieRepository;
        this.searchService = searchService;
        this.messageStateRepository = messageStateRepository;
        this.sequanceService = sequanceService;
    }


    public List<Button> getStateByPath(String path, Long chatId, Integer messageId){
        MessageState messageState = messageStateRepository.getByChatIdAndMessageId(chatId,messageId);
        List<String> pathList = Arrays.stream(path.split("#")).collect(Collectors.toList());
        if(messageState == null){
            Movie movie = movieRepository.findById(Integer.valueOf(pathList.get(0)));
            if( movie == null)
                movie = searchService.getMovie(Integer.valueOf(pathList.get(0)));
            messageState = new MessageState();
            messageState.setChatId(chatId);
            messageState.setMessageId(messageId);
            messageState.setMovieFileHierarchy(movie.getMovieFileHierarchy());
            messageState.setId(sequanceService.getNextSequence(MessageState.class));
        }
        messageState.setHierarhyPath(pathList);
        messageStateRepository.save(messageState);

        int i = 1;
        Map<Integer, MovieFileHierarchy> currentNodeMap = messageState.getMovieFileHierarchy();
        while (i < pathList.size()){
            MovieFileHierarchy currentHierarchy = currentNodeMap.get(Integer.valueOf(pathList.get(i)));
            if(currentHierarchy.getChildren() != null){
                currentNodeMap = currentHierarchy.getChildren();
            }
            i++;
        }
        ArrayList<Button> result = new ArrayList<>();
        if(pathList.size() > 1){
            Button backButton = new Button();
            backButton.setName("Back");
            backButton.setData(pathList.stream().limit((long) (pathList.size() - 1)).reduce((s, s2) -> s + "#" + s2).orElse(""));
            result.add(backButton);
        }
        currentNodeMap.forEach((key,value) -> {
            Button button = new Button();
            button.setName(value.getName());
            if(value.getType() == MovieFileHierarchy.FileHierarchyType.LINK){
                button.setUrl(value.getUrl());
            }else{
                button.setData(path + "#" + key);
            }
            result.add(button);
        });
        return result;
    }


}
