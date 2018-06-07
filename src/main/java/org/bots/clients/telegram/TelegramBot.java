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
            voiceSearch(update.getMessage(), update.getMessage().getChatId());
        } else if (update.hasCallbackQuery()) {
            if (!update.getCallbackQuery().getData().isEmpty()) {
                CallbackQuery callback = update.getCallbackQuery();
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
                    sendSearchResponse(update.getMessage().getText(),update.getMessage().getChatId());
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

    private void sendSearchResponse(String param, Long chatId){

        List<MovieSearchResponse> list = searchService.searchMovie(param);
        if(list.size() == 1){
            sendMovie(list.get(0).getId(),chatId);
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

    private EditMessageReplyMarkup createReplyButtons(List<Button> buttons, String menuType){
        EditMessageReplyMarkup editMessage = new EditMessageReplyMarkup();
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        editMessage.setReplyMarkup(inlineKeyboardMarkup);

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


        List<List<InlineKeyboardButton>> replyInlineButtons = Lists.partition(responseInlineButtons, 2);

        List<List<InlineKeyboardButton>> inlineButtons = new ArrayList<>();
        inlineButtons.add(inlineMenuButtons);
        inlineButtons.addAll(replyInlineButtons);
        inlineKeyboardMarkup.setKeyboard(inlineButtons);
        return editMessage;

    }

    private Button createFavoriteButton(TelegramClient client, String movieId){
        boolean isFavorite = userService.checkFavorite(client, Integer.valueOf(movieId));
        if(isFavorite)
            return Button.favoriteButton(movieId, false);
        else
            return Button.favoriteButton(movieId, true);
    }


    private void sendMovie(String movieId, Long chatId) {
        SendPhoto msg = openMovie(movieId);
        msg.setChatId(chatId);
        try {
            sendPhoto(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
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
                    .setCallbackData(OPEN_COMMAND + "#" + movieId + "#" + movieFileHierarchy.getName().hashCode());
        }
        keyboard.setKeyboard(Lists.partition(buttons, 2));
        return message;
    }

    private List<String> getCallbackMessage(String data){
        List<String> result = Arrays.asList(data.split("#"));
        return result.subList(1, result.size());
    }

    private void voiceSearch(Message msg, Long chatId){
        String searchText = null;
        GetFile getFileMethod = new GetFile();
        getFileMethod.setFileId(msg.getVoice().getFileId());
        try {
            File file = downloadFile(execute(getFileMethod).getFilePath());
            searchText = voiceRecognitionService.recognizeVoice(file);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        sendSearchResponse(searchText, chatId);
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
                sendSearchResponse(commandParams, message.getChatId());
            }
        };
    }

    private Consumer<CallbackQuery> searchCallbackHandler() {
        return callbackQuery -> {
            sendCallbackResponse(callbackQuery.getId());
            sendMovie(callbackQuery.getData().toLowerCase().replace(SEARCH_COMMAND + "#", ""),
                    callbackQuery.getMessage().getChatId());
        };
    }

    private Consumer<Message> openHandler() {
        return message -> openMovie(message.getText().toLowerCase().replace(OPEN_COMMAND, ""));
    }

    private Consumer<CallbackQuery> openCallbackHandler() {
        return callbackQuery -> {
            sendCallbackResponse(callbackQuery.getId());
            String data = callbackQuery.getData().toLowerCase().replace(OPEN_COMMAND + "#","");
            List<Button> buttons = messageStateService.getStateByPath(
                    data,
                    callbackQuery.getMessage().getChatId(),
                    callbackQuery.getMessage().getMessageId());
            buttons.add(createFavoriteButton(TelegramClient.of(callbackQuery.getFrom().getId()), data.split("#")[0]));
            EditMessageReplyMarkup msg = createReplyButtons(buttons, OPEN_COMMAND);
            msg.setChatId(callbackQuery.getMessage().getChatId());
            msg.setMessageId(callbackQuery.getMessage().getMessageId());
            sendMethod(msg);

        };
    }


    private Consumer<CallbackQuery> favoriteCallbackHandler() {
        return callbackQuery ->
                userService.changeFavorites(TelegramClient.of(callbackQuery.getFrom().getId()),
                        user -> {
                            List<String> data = getCallbackMessage(callbackQuery.getData());
                            if (data.get(0).equalsIgnoreCase("add")) {
                                user.getFavorites().add(Integer.valueOf(data.get(1)));
                            } else {
                                user.getFavorites().remove(Integer.valueOf(data.get(1)));
                            }
                        });
    }

}
