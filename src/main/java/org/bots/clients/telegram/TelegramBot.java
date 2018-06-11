package org.bots.clients.telegram;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.bots.model.datebase.Movie;
import org.bots.model.datebase.TelegramClient;
import org.bots.model.items.Button;
import org.bots.model.items.MovieFileHierarchy;
import org.bots.model.items.MovieSearchResponse;
import org.bots.services.MessageStateService;
import org.bots.services.SearchService;
import org.bots.services.UserService;
import org.bots.services.VoiceRecognitionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.GetFile;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.bots.common.Constants.BOT_NAME;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    private final SearchService searchService;
    private final MessageStateService messageStateService;
    private final VoiceRecognitionService voiceRecognitionService;
    private final UserService userService;

    public static final String SEARCH_COMMAND = "/search";
    public static final String START_COMMAND = "/start";
    public static final String OPEN_COMMAND = "/open";
    public static final String OPEN_WITH_TEXT_CALLBACK = "/openwithtext";
    public static final String FAVORITE_COMMAND = "/favorite";

    private final Map<String, Consumer<Message>> botCommands = new HashMap<>();
    private final Map<String, Consumer<CallbackQuery>> callbacks = new HashMap<>();

    @Value("${telegram.token}")
    private String token;

    public TelegramBot(SearchService searchService, MessageStateService messageStateService, VoiceRecognitionService voiceRecognitionService, UserService userService) {
        this.searchService = searchService;
        this.messageStateService = messageStateService;
        this.voiceRecognitionService = voiceRecognitionService;
        this.userService = userService;

        botCommands.put(START_COMMAND, startHandler());

        botCommands.put(SEARCH_COMMAND, searchHandler());
        callbacks.put(SEARCH_COMMAND, searchCallbackHandler());
        botCommands.put(OPEN_COMMAND, openHandler());
        callbacks.put(OPEN_COMMAND, openCallbackHandler());
        callbacks.put(OPEN_WITH_TEXT_CALLBACK, openWithTextCallbackHandler());


        botCommands.put(FAVORITE_COMMAND, favoriteHandler());
        callbacks.put(FAVORITE_COMMAND, favoriteCallbackHandler());
    }

    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().isCommand()) {
            Message msg = update.getMessage();
            String[] tokens = msg.getText().split(" ");
            String command = tokens[0].toLowerCase();
            Consumer handler = this.botCommands.get(command);
            if( handler == null)
                sendMessage("Unknown command",update.getMessage().getChatId());
            else
                handler.accept(msg);

        } else if (update.hasMessage() && update.getMessage().getVoice() != null) {
            voiceSearch(update.getMessage(), update.getMessage().getChatId(), update.getMessage().getFrom().getId());
        } else if (update.hasCallbackQuery()) {
            if (!update.getCallbackQuery().getData().isEmpty()) {
                CallbackQuery callback = update.getCallbackQuery();
                sendCallbackResponse(callback.getId());
                String[] tokens = callback.getData().split("#");
                String command = tokens[0].toLowerCase();
                Consumer handler = this.callbacks.get(command);
                if(handler != null)
                    handler.accept(callback);
            }
        } else {
            if (update.hasMessage() && update.getMessage().hasText()) {
                String lastUserCommand = userService.takeActiveCommand(TelegramClient.of(update.getMessage().getFrom().getId()));
                if (lastUserCommand == null) {
                    sendMessage("Input command first, to see all command type /commands", update.getMessage().getChatId());
                } else {
                    sendSearchResponse(update.getMessage().getText(),update.getMessage().getChatId(), update.getMessage().getFrom().getId());
                }}
        }
    }

    private void sendMethod(BotApiMethod method){
        try {
            execute(method);
        } catch (TelegramApiException e) {
            log.error("Cant execute method ", e);
        }
    }

    private void sendMessage(String message, Long chatId){
        SendMessage msg = new SendMessage();
        msg.setText(message);
        msg.setChatId(chatId);
        sendMethod(msg);
    }

    private List<String> getCommandParamsFromText(String text){
        List<String> params = Arrays.asList(text.split(" "));
        return params.size() > 1 ? params.subList(1,params.size()) : Collections.emptyList();
    }

    private void sendSearchResponse(String param, Long chatId, Integer userId){

        List<MovieSearchResponse> list = searchService.searchMovie(param);
        if(list.size() == 1){
            sendMovie(list.get(0).getId(),chatId, userId);
            return;
        }

        SendMessage message = new SendMessage();

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
                    .setCallbackData(SEARCH_COMMAND + "#" + searchItem.getId());
        }
        message.setChatId(chatId);
        sendMethod(message);
    }

    private void sendCallbackResponse(String id){
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(id);
        sendMethod(answer);
    }

    private EditMessageReplyMarkup createEditMessageReplyMarkup(List<Button> buttons, String menuType, Integer columns){
        EditMessageReplyMarkup editMessage = new EditMessageReplyMarkup();
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        editMessage.setReplyMarkup(inlineKeyboardMarkup);

        List<List<InlineKeyboardButton>> inlineButtons =  createInlineButtons(buttons, menuType, columns);
        inlineKeyboardMarkup.setKeyboard(inlineButtons);
        return editMessage;
    }

    private List<List<InlineKeyboardButton>> createInlineButtons(List<Button> buttons, String menuType, Integer columns){


        List<Button> menuButtons = buttons.stream()
                .filter(btn -> btn.getType() == Button.ButtonType.MENU)
                .collect(Collectors.toList());
        List<Button> responseButtons = buttons.stream()
                .filter(btn -> btn.getType() == Button.ButtonType.RESPONSE)
                .collect(Collectors.toList());

        List<InlineKeyboardButton> inlineMenuButtons =  menuButtons.stream().map(btn -> {
            InlineKeyboardButton inlineBtn = new InlineKeyboardButton();
            inlineBtn.setText(btn.getName());
            inlineBtn.setCallbackData((btn.getMenuType() == null ? menuType : btn.getMenuType()) + "#" + btn.getData());
            return inlineBtn;
        }).collect(Collectors.toList());

        List<InlineKeyboardButton> responseInlineButtons = responseButtons.stream()
                .sorted(Comparator.nullsLast(Comparator.comparing(Button::getOrder)))
                .map(button -> {
                    InlineKeyboardButton inlineButton = new InlineKeyboardButton();
                    inlineButton.setText(button.getName());
                    if(button.getData() != null)
                        inlineButton.setCallbackData(menuType + "#" + button.getData());
                    else if(button.getUrl() != null)
                        inlineButton.setUrl(button.getUrl());
                    return inlineButton;
                }).collect(Collectors.toList());


        List<List<InlineKeyboardButton>> replyInlineButtons = Lists.partition(responseInlineButtons, columns);

        List<List<InlineKeyboardButton>> inlineButtons = new ArrayList<>();
        inlineButtons.add(inlineMenuButtons);
        inlineButtons.addAll(replyInlineButtons);
        return inlineButtons;
    }

    private Button createFavoriteButton(TelegramClient client, String data){
        String movieId = data.split("#")[0];
        boolean isFavorite = userService.checkFavorite(client, Integer.valueOf(movieId));
        return Button.favoriteButton(data, isFavorite);

    }


    private void sendMovie(String movieId, Long chatId, Integer userId) {
        SendPhoto msg = openMovie(movieId, chatId, userId);

        try {
            sendPhoto(msg);
        } catch (TelegramApiException e) {
            log.error("Cant send movie", e);
        }
    }

    private SendPhoto openMovie(String movieId, Long chatId, Integer userId){
        Movie movie = searchService.getAndSaveMovie(Integer.valueOf(movieId));

        SendPhoto msg = new SendPhoto();
        msg.setChatId(chatId);
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

        msg.setCaption(str.toString());
        msg.setParseMode(ParseMode.MARKDOWN);
        msg.setPhoto(movie.getPoster());

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        msg.setReplyMarkup(keyboard);


        List<Button> buttons = new ArrayList<>();

        boolean isFavorite = userService.checkFavorite(TelegramClient.of(userId), Integer.valueOf(movieId));
        buttons.add(Button.favoriteButton(movieId, isFavorite));

        for(MovieFileHierarchy movieFileHierarchy : movie.getMovieFileHierarchy().values() ) {
            Button btn = new Button();
            btn.setType(Button.ButtonType.RESPONSE);
            btn.setName(movieFileHierarchy.getName());
            btn.setMenuType(OPEN_COMMAND);
            btn.setData(movieId + "#" + movieFileHierarchy.getName().hashCode());
            buttons.add(btn);
        }

        keyboard.setKeyboard(createInlineButtons(buttons, OPEN_COMMAND, 2));
        return msg;
    }

    private List<String> getCallbackMessage(String data){
        List<String> result = Arrays.asList(data.split("#"));
        return result.subList(1, result.size());
    }

    private void voiceSearch(Message msg, Long chatId, Integer userId){
        String searchText = null;
        GetFile getFileMethod = new GetFile();
        getFileMethod.setFileId(msg.getVoice().getFileId());
        try {
            File file = downloadFile(execute(getFileMethod).getFilePath());
            searchText = voiceRecognitionService.recognizeVoice(file);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        sendSearchResponse(searchText, chatId, userId);
    }

    private Consumer<Message> startHandler() {
        return msg -> {
            userService.registerUser(msg.getFrom().getFirstName(), msg.getFrom().getUserName(), TelegramClient.of(msg.getFrom().getId()));
            SendMessage message = new SendMessage();
            String html = "*Welcome to search movie bot, let's try search something*";
            message.setText(html);
            message.setChatId(msg.getChatId());
            ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
            List<KeyboardRow> keyboard = new ArrayList<>();
            KeyboardRow row = new KeyboardRow();
            row.add(FAVORITE_COMMAND);
            row.add("/Settings");
            row.add(SEARCH_COMMAND);
            keyboard.add(row);
            row = new KeyboardRow();
            row.add("/Films");
            row.add("/Serials");
            row.add("/MultFilms");
            keyboard.add(row);
            keyboardMarkup.setKeyboard(keyboard);
            message.setReplyMarkup(keyboardMarkup);
            message.setParseMode(ParseMode.MARKDOWN);
            sendMethod(message);
        };
    }

    private Consumer<Message> searchHandler() {
        return message -> {
            String commandParams = message.getText().toLowerCase().replace(SEARCH_COMMAND, "");
            if(commandParams.length() == 0){
                userService.setActiveCommand(TelegramClient.of(message.getFrom().getId()), SEARCH_COMMAND);

                sendMessage("Enter movie name to search", message.getChatId());
            }else{
                sendSearchResponse(commandParams, message.getChatId(), message.getFrom().getId());
            }
        };
    }

    private Consumer<CallbackQuery> searchCallbackHandler() {
        return callbackQuery -> {
            sendMovie(callbackQuery.getData().toLowerCase().replace(SEARCH_COMMAND + "#", ""),
                    callbackQuery.getMessage().getChatId(), callbackQuery.getFrom().getId());
        };
    }

    private Consumer<Message> openHandler() {
        return message -> sendMovie(message.getText().toLowerCase().replace(OPEN_COMMAND, ""),message.getChatId(), message.getFrom().getId());
    }

    private Consumer<CallbackQuery> openCallbackHandler() {
        return callbackQuery -> {
            String data = callbackQuery.getData().toLowerCase().replace(OPEN_COMMAND + "#","");
            EditMessageReplyMarkup msg = createEditMessageReplyMarkup(data, callbackQuery.getFrom().getId(),
                    callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId());
            sendMethod(msg);
        };
    }

    private Consumer<CallbackQuery> openWithTextCallbackHandler() {
        return callbackQuery -> sendMovie(callbackQuery.getData().replace(OPEN_WITH_TEXT_CALLBACK + "#",""),callbackQuery.getMessage().getChatId(), callbackQuery.getFrom().getId());
    }

    private EditMessageReplyMarkup createEditMessageReplyMarkup(String data, Integer userId, Long chatId, Integer messageId){
        List<Button> buttons = messageStateService.getStateByPath(data, chatId, messageId);
        buttons.add(createFavoriteButton(TelegramClient.of(userId), data));

        EditMessageReplyMarkup msg = createEditMessageReplyMarkup(buttons, OPEN_COMMAND, 2);
        msg.setChatId(chatId);
        msg.setMessageId(messageId);
        return msg;
    }

    private Consumer<Message> favoriteHandler() {
        return message -> {
            List<Integer> favorites = userService.getFavorites(TelegramClient.of(message.getFrom().getId()));
            List<Movie> favoriteMovies = searchService.getMovieByIdList(favorites);

            SendMessage msg = new SendMessage();
            msg.setChatId(message.getChatId());
            msg.setText("*You favorite list size: *" + favoriteMovies.size());
            msg.setParseMode(ParseMode.MARKDOWN);

            List<Button> buttons = createFavoriteButtons(favoriteMovies);
            List<List<InlineKeyboardButton>> inlineButons = createInlineButtons(buttons, OPEN_WITH_TEXT_CALLBACK, 1);
            InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
            keyboard.setKeyboard(inlineButons);
            msg.setReplyMarkup(keyboard);

            sendMethod(msg);
        };
    }

    private Consumer<CallbackQuery> favoriteCallbackHandler() {
        return callbackQuery -> {
            userService.changeFavorites(TelegramClient.of(callbackQuery.getFrom().getId()),
                    user -> {
                        List<String> data = getCallbackMessage(callbackQuery.getData());
                        if(user.getFavorites().contains(Integer.valueOf(data.get(0))))
                            user.getFavorites().remove(Integer.valueOf(data.get(0)));
                        else
                            user.getFavorites().add(Integer.valueOf(data.get(0)));
                    }
            );
            EditMessageReplyMarkup msg = createEditMessageReplyMarkup(callbackQuery.getData().replace(FAVORITE_COMMAND + "#", ""), callbackQuery.getFrom().getId(), callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId());
            sendMethod(msg);
        };
    }

    private List<Button> createFavoriteButtons(List<Movie> movieIds){
        List<Button> result = new ArrayList<>();
        if(movieIds != null && !movieIds.isEmpty())
            movieIds.forEach(movie -> {
                Button btn = new Button();
                btn.setType(Button.ButtonType.RESPONSE);
                btn.setData(movie.getId().toString());
                btn.setName(movie.getTitle());
                result.add(btn);
            });
        return result;
    }

}
