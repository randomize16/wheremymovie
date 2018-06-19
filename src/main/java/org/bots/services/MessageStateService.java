package org.bots.services;

import lombok.AllArgsConstructor;
import org.bots.model.datebase.MessageState;
import org.bots.model.datebase.Movie;
import org.bots.model.datebase.MovieFile;
import org.bots.model.datebase.MovieFileHierarchy;
import org.bots.model.items.Button;
import org.bots.repository.MessageStateRepository;
import org.bots.repository.MovieRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class MessageStateService {

    private final MovieRepository movieRepository;
    private final SearchService searchService;
    private final MessageStateRepository messageStateRepository;
    private final NextSequenceService sequenceService;

    public List<Button> getButtonsByPath(String path, Long chatId, Integer messageId){
        MessageState messageState = messageStateRepository.getByChatIdAndMessageId(chatId,messageId);
        List<String> pathList = Arrays.stream(path.split("#")).collect(Collectors.toList());
        if(messageState == null){
            Movie movie = movieRepository.findById(Integer.valueOf(pathList.get(0)));
            if( movie == null)
                movie = searchService.getAndSaveMovie(Integer.valueOf(pathList.get(0)));
            messageState = new MessageState();
            messageState.setChatId(chatId);
            messageState.setMessageId(messageId);
            messageState.setId(sequenceService.getNextSequence(MessageState.class));
            if(movie.getWithPlaylist())
                messageState.setMovieFileHierarchies(createPlaylistTree(movie.getMovieFiles()));
            else
                messageState.setMovieFileHierarchies(createNonPlaylistTree(movie.getMovieFiles()));
        }
        messageState.setHierarchyPath(pathList);
        messageStateRepository.save(messageState);

        int i = 1;

        Map<Integer, MovieFileHierarchy> currentNodeMap = messageState.getMovieFileHierarchies();
        while (i < pathList.size()){
            MovieFileHierarchy currentHierarchy = currentNodeMap.get(Integer.valueOf(pathList.get(i)));
            if(currentHierarchy.getChildren() != null){
                currentNodeMap = currentHierarchy.getChildren();
            }
            i++;
        }        ArrayList<Button> result = new ArrayList<>();
        if(pathList.size() > 1){
            Button backButton = new Button();
            backButton.setName("Back");
            backButton.setType(Button.ButtonType.RESPONSE);
            backButton.setOrder(-1);
            backButton.setData(pathList.stream().limit((long) (pathList.size() - 1)).reduce((s, s2) -> s + "#" + s2).orElse(""));
            result.add(backButton);
        }
        currentNodeMap.forEach((key,value) -> {
            Button button = new Button();
            button.setName(value.getName());
            button.setType(Button.ButtonType.RESPONSE);
            if(value.getType() == MovieFileHierarchy.FileHierarchyType.LINK){
                button.setUrl(value.getUrl());
            }else{
                button.setData(path + "#" + key);
            }
            result.add(button);
        });


        return result;
    }

    private Map<Integer, MovieFileHierarchy> createPlaylistTree(List<MovieFile> movieFiles){
        Map<Integer, MovieFileHierarchy> result = new LinkedHashMap<>();

        Map<Integer, List<MovieFile>> groupBySeason = movieFiles.stream().collect(Collectors.groupingBy(MovieFile::getSeason, Collectors.toList()));
        groupBySeason.forEach((season, seasonMfList) ->{
            MovieFileHierarchy seasonHierarchy = MovieFileHierarchy.createSeason(String.valueOf(season));
            result.put( season, seasonHierarchy);
            seasonHierarchy.setOrder(season);
            seasonHierarchy.setChildren(new LinkedHashMap<>());
            Map<String, List<MovieFile>> groupingByTranslation = seasonMfList.stream()
                    .collect(Collectors.groupingBy(o -> o.getTranslation() + " - " + o.getSource(), Collectors.toList()));
            AtomicInteger translationOrder = new AtomicInteger();
            groupingByTranslation.forEach((translation, seriesList) -> {
                MovieFileHierarchy translationHierarchy = MovieFileHierarchy.createTranslation(translation);
                seasonHierarchy.getChildren().put(Objects.hash(translation), translationHierarchy);
                translationHierarchy.setOrder(translationOrder.getAndIncrement());
                translationHierarchy.setChildren(new LinkedHashMap<>());


                seriesList.forEach(series -> {
                    MovieFileHierarchy seriesHierarchy = MovieFileHierarchy.createSeries(String.valueOf(series.getSeries()));
                    translationHierarchy.getChildren().put(series.getSeries(), seriesHierarchy);
                    seriesHierarchy.setOrder(series.getSeries());
                    seriesHierarchy.setChildren(new LinkedHashMap<>());

                    AtomicInteger linkOrder = new AtomicInteger();
                    series.getUrls().forEach((name, url) -> {
                        MovieFileHierarchy linkHierarchy = MovieFileHierarchy.createLink(name, url);
                        linkHierarchy.setOrder(linkOrder.getAndIncrement());
                        seriesHierarchy.getChildren().put(Objects.hash(name), linkHierarchy);
                    });
                });
            });

        });

        return result;
    }

    private  Map<Integer, MovieFileHierarchy> createNonPlaylistTree(List<MovieFile> movieFiles){
        Map<Integer, MovieFileHierarchy>  result = new LinkedHashMap<>();
            movieFiles.forEach(movieFile -> {
                MovieFileHierarchy filmHierarchy = MovieFileHierarchy.createTranslation(movieFile.getName());
                result.put(Objects.hash(movieFile.getName()), filmHierarchy);

                filmHierarchy.setChildren(new LinkedHashMap<>());
                AtomicInteger linkOrder = new AtomicInteger();
                movieFile.getUrls().forEach((name, url) -> {
                    MovieFileHierarchy linkHierarchy = MovieFileHierarchy.createLink(name, url);
                    linkHierarchy.setOrder(linkOrder.getAndIncrement());
                    filmHierarchy.getChildren().put(Objects.hash(name), linkHierarchy);
                });
            });
        return result;
    }

    public List<Button> getInitialButtons(Movie movie){
        List<Button> result = new ArrayList<>();
        List<String> firstLevel;
        if(movie.getWithPlaylist()){
            firstLevel = movie.getMovieFiles().stream()
                    .map(MovieFile::getSeason).distinct().map(String::valueOf).collect(Collectors.toList());
        }else {
            firstLevel = movie.getMovieFiles().stream().map(MovieFile::getName).collect(Collectors.toList());
        }
        firstLevel.forEach(season -> {
            Button btn = new Button();
            btn.setType(Button.ButtonType.RESPONSE);
            btn.setName(String.valueOf(season));
            btn.setOrder(0);
            btn.setData(movie.getId() + "#" + season);
            result.add(btn);
        });
        return result;
    }

}
