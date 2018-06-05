package org.bots.clients.telegram;

import com.google.common.collect.Lists;
import org.bots.configuration.ApplicationVariables;
import org.bots.core.MessageStateService;
import org.bots.core.VoiceRecognitionService;
import org.bots.model.datebase.Movie;
import org.bots.model.items.Button;
import org.bots.model.items.MovieFileHierarchy;
import org.bots.model.items.MovieSearchResponse;
import org.bots.sources.SearchService;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.GetFile;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.bots.configuration.ApplicationVariables.CREATOR_ID;
import static org.telegram.abilitybots.api.objects.Flag.CALLBACK_QUERY;
import static org.telegram.abilitybots.api.objects.Locality.ALL;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;

@Component
public class TelegramBot extends AbilityBot {

    private final SearchService searchService;
    private final MessageStateService messageStateService;
    private final VoiceRecognitionService voiceRecognitionService;

    private static final String SEARCH_REPLY = "search#";
    private static final String OPEN_REPLY = "open#";

    public TelegramBot(SearchService searchService, MessageStateService messageStateService, VoiceRecognitionService voiceRecognitionService) {
        super(ApplicationVariables.TELEGRAM_TOKEN, ApplicationVariables.BOT_NAME);
        this.searchService = searchService;
        this.messageStateService = messageStateService;
        this.voiceRecognitionService = voiceRecognitionService;
    }


    @Override
    public int creatorId() {
        return CREATOR_ID;
    }

    public Ability getStarted(){
        return Ability
                .builder()
                .name("start")
                .info("Let's find some movie")
                .locality(ALL)
                .privacy(PUBLIC)
                .input(0)
                .action(starterHandler())
                .build();
    }

    private Consumer<MessageContext> starterHandler() {
        return msgCtx -> {
            SendMessage message = new SendMessage();
            String html = "*Welcome to search movie bot, let's try search something*";
            message.setText(html);
            message.setChatId(msgCtx.chatId());
            ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
            List<KeyboardRow> keyboard = new ArrayList<>();
            KeyboardRow row = new KeyboardRow();
                row.add("/Favorites");
                row.add("/Settings");
                row.add("/Search");
            keyboard.add(row);
            row = new KeyboardRow();
                row.add("/Films");
                row.add("/Serials");
                row.add("/MultFilms");
            keyboard.add(row);
            keyboardMarkup.setKeyboard(keyboard);
            message.setReplyMarkup(keyboardMarkup);
            message.setParseMode(ParseMode.MARKDOWN);
            silent.execute(message);
        };
    }

    public Ability voiceSearch(){
        return Ability
                .builder()
                .name(DEFAULT)
                .info("Voice search")
                .privacy(PUBLIC)
                .locality(ALL)
                .flag(update -> update.getMessage().getVoice() != null)
                .action(msgCtx -> {
                    String searchText = null;
                    GetFile getFileMethod = new GetFile();
                    getFileMethod.setFileId(msgCtx.update().getMessage().getVoice().getFileId());
                    try {
                        File file = downloadFile(execute(getFileMethod).getFilePath());
                        searchText = voiceRecognitionService.recognizeVoice(file);
                        } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    SendMessage msg = searchMovie(searchText);
                    msg.setChatId(msgCtx.chatId());
                    silent.execute(msg);
                })

                .build();
    }

    public Ability searchMovie(){
        return Ability
                .builder()
                .name("search")
                .info("Search movie")
                .locality(ALL)
                .privacy(PUBLIC)
                .action(searchHandler())
                .reply(update -> {
                    sendCallbackResponse(update.getCallbackQuery().getId());
                    SendPhoto msg = openMovie(update.getCallbackQuery().getData().replace(SEARCH_REPLY, ""));
                    msg.setChatId(update.getCallbackQuery().getMessage().getChatId());
                    try {
                        sendPhoto(msg);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }, CALLBACK_QUERY, isStartWith(SEARCH_REPLY))
                .build();
    }

    private EditMessageReplyMarkup createReplyButtons(List<Button> buttons, String menuType){
        EditMessageReplyMarkup editMessage = new EditMessageReplyMarkup();
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        editMessage.setReplyMarkup(inlineKeyboardMarkup);

        List<InlineKeyboardButton> inlineButtons = new ArrayList<>();
        buttons.forEach(button -> {
            InlineKeyboardButton inlineButton = new InlineKeyboardButton();
            inlineButton.setText(button.getName());
            if(button.getData() != null)
                inlineButton.setCallbackData(menuType + button.getData());
            else if(button.getUrl() != null)
                inlineButton.setUrl(button.getUrl());
            inlineButtons.add(inlineButton);
        });

        inlineKeyboardMarkup.setKeyboard(Lists.partition(inlineButtons,2));
        return editMessage;

    }

    public Ability openMovie(){
        return Ability
                .builder()
                .name("open")
                .info("Open movie by id")
                .locality(ALL)
                .privacy(PUBLIC)
                .action(msg -> openMovie(Arrays.stream(msg.arguments()).reduce((s, s2) -> s + " " + s2).orElse("")))
                .reply(update -> {
                    sendCallbackResponse(update.getCallbackQuery().getId());
                    List<Button> buttons = messageStateService.getStateByPath(update.getCallbackQuery().getData().replace(OPEN_REPLY,""),
                            update.getCallbackQuery().getMessage().getChatId(),
                            update.getCallbackQuery().getMessage().getMessageId());
                    EditMessageReplyMarkup msg = createReplyButtons(buttons, OPEN_REPLY);
                    msg.setChatId(update.getCallbackQuery().getMessage().getChatId());
                    msg.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
                    silent.executeAsync(msg);
                }, CALLBACK_QUERY, isStartWith(OPEN_REPLY))
                .build();
    }

    private Consumer<MessageContext> searchHandler() {
        return msgCtx -> {
            SendMessage msg = searchMovie(Arrays.stream(msgCtx.arguments()).reduce((s, s2) -> s + " " + s2).orElse(""));
            msg.setChatId(msgCtx.chatId());
            silent.execute(msg);

        };
    }

    private Predicate<Update> isStartWith(String type) {
        return upd -> {
            String data = upd.getCallbackQuery().getData();
            return !data.isEmpty() && data.startsWith(type);
        };
    }

    private void sendCallbackResponse(String id){
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(id);
        silent.executeAsync(answer);
    }

    private SendMessage searchMovie(String param){
        SendMessage message = new SendMessage();


        List<MovieSearchResponse> list = searchService.searchMovie(param);
        StringBuilder text = new StringBuilder();
        text.append("*Search movie* ").append(param).append(" \n");
        int rank = 1;
        for(MovieSearchResponse movie : list){
            if(text.length() != 0)
                text.append("------------------------------------------").append("\n");
                text.append("_").append(rank++).append("_ ")
                    .append("*Title:*").append(movie.getTitle()).append("\n")
                    .append("\t*Year* :").append(movie.getYear()).append("\n")
                    .append("\t*Original* :").append(movie.getOriginalName()).append("\n");
        }

        message.setText(text.toString());
        message.setParseMode(ParseMode.MARKDOWN);

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        message.setReplyMarkup(keyboard);
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        keyboard.setKeyboard(Collections.singletonList(buttons));
        rank = 1;
        for(MovieSearchResponse searchItem: list ) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            buttons.add(button);
            button.setText(String.valueOf(rank++))
                  .setCallbackData(SEARCH_REPLY + searchItem.getId());
        }

        return message;
    }

    private SendPhoto openMovie(String movieId){
        Movie movie = searchService.getMovie(Integer.valueOf(movieId));

        SendPhoto message = new SendPhoto();
        StringBuilder str = new StringBuilder();
        str
                .append("*Title*: ").append(movie.getTitle()).append("\n")
                .append("*OriginalTitle*: ").append(movie.getOriginalTitle().replace("\n", "")).append("\n")
                .append("*Category*: ").append(movie.getCategories().stream().reduce((s, s2) -> s+ ", " + s2).orElse("")).append("\n")
                .append("*Date*: ").append(movie.getDate()).append("\n")
                .append("*IMDB*: ").append(movie.getRatio()).append("\n");
//                .append("*Director*: ").append(movie.getDirectors().stream().reduce((s, s2) -> s+ ", " + s2).orElse("")).append("\n")
//                .append("*Actors*: ").append(movie.getCasts().stream().reduce((s, s2) -> s+ ", " + s2).orElse("")).append("\n")
//                .append("*Description*: ").append(movie.getDescription()).append("\n");

        message.setCaption(str.toString());
        message.setParseMode(ParseMode.MARKDOWN);
        message.setPhoto(movie.getPoster());

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        message.setReplyMarkup(keyboard);
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        for(MovieFileHierarchy movieFileHierarchy : movie.getMovieFileHierarchy().values() ) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            buttons.add(button);
                button.setText(movieFileHierarchy.getName())
                        .setCallbackData(OPEN_REPLY + movieId + "#" + movieFileHierarchy.getName().hashCode());

        }
        keyboard.setKeyboard(Lists.partition(buttons, 2));
        return message;
    }


}
