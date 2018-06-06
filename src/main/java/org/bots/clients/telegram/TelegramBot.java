package org.bots.clients.telegram;

import com.google.common.collect.Lists;
import org.bots.configuration.ApplicationVariables;
import org.bots.model.datebase.Movie;
import org.bots.model.datebase.TelegramClient;
import org.bots.model.items.Button;
import org.bots.model.items.MovieFileHierarchy;
import org.bots.model.items.MovieSearchResponse;
import org.bots.services.MessageStateService;
import org.bots.services.SearchService;
import org.bots.services.UserService;
import org.bots.services.VoiceRecognitionService;
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
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.bots.configuration.ApplicationVariables.CREATOR_ID;
import static org.telegram.abilitybots.api.objects.Flag.CALLBACK_QUERY;
import static org.telegram.abilitybots.api.objects.Locality.ALL;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;

@Component
public class TelegramBot extends AbilityBot {

    private final SearchService searchService;
    private final MessageStateService messageStateService;
    private final VoiceRecognitionService voiceRecognitionService;
    private final UserService userService;

    private static final String SEARCH_REPLY = "search#";
    private static final String OPEN_REPLY = "open#";
    private static final String FAVORITE_REPLY = "favorite#";


    private static final String SEARCH_COMMAND = "search";

    public TelegramBot(SearchService searchService, MessageStateService messageStateService, VoiceRecognitionService voiceRecognitionService, UserService userService) {
        super(ApplicationVariables.TELEGRAM_TOKEN, ApplicationVariables.BOT_NAME);
        this.searchService = searchService;
        this.messageStateService = messageStateService;
        this.voiceRecognitionService = voiceRecognitionService;
        this.userService = userService;
    }


    @Override
    public int creatorId() {
        return CREATOR_ID;
    }

    public Ability commandStart(){
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


    public Ability commonTextHandler(){
        return Ability
                .builder()
                .name(DEFAULT)
                .info("Voice search")
                .privacy(PUBLIC)
                .locality(ALL)
                .action(msgCtx -> {
                    if(msgCtx.update().getMessage().getVoice() != null){
                        String searchText = null;
                        GetFile getFileMethod = new GetFile();
                        getFileMethod.setFileId(msgCtx.update().getMessage().getVoice().getFileId());
                        try {
                            File file = downloadFile(execute(getFileMethod).getFilePath());
                            searchText = voiceRecognitionService.recognizeVoice(file);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        sendSearchResponse(searchText, msgCtx.chatId());
                    } else if(msgCtx.update().hasMessage() && msgCtx.update().getMessage().hasText()){
                        String lastUserCommand = userService.takeActiveCommand(TelegramClient.of(msgCtx.user().id()));
                        if(lastUserCommand == null) {
                            silent.send("Input command first, to see all command type /commands", msgCtx.chatId());
                        } else {
                            sendSearchResponse(msgCtx.update().getMessage().getText(),msgCtx.chatId());
                        }
                    }
                })
                .reply(update -> {
                    userService.changeFavorites(TelegramClient.of(update.getCallbackQuery().getFrom().getId()),
                            user -> {
                                List<String> data = getCallbackMessage(update.getCallbackQuery().getData());
                                if(data.get(0).equalsIgnoreCase("add")){
                                    user.getFavorites().add(Integer.valueOf(data.get(1)));
                                }else{
                                    user.getFavorites().remove(Integer.valueOf(data.get(1)));
                                }
                            });
                }, CALLBACK_QUERY, isStartWith(FAVORITE_REPLY))
                .build();
    }

    public Ability commandSearch(){
        return Ability
                .builder()
                .name(SEARCH_COMMAND)
                .info("Search movie")
                .locality(ALL)
                .privacy(PUBLIC)
                .action(msgCtx -> {
                    if(msgCtx.arguments().length == 0){
                        userService.setActiveCommand(TelegramClient.of(msgCtx.user().id()), SEARCH_COMMAND);
                        silent.send("Enter movie name to search", msgCtx.chatId());
                    }else{
                        sendSearchResponse(Arrays.stream(msgCtx.arguments()).reduce((s, s2) -> s + " " + s2).orElse(""),
                                msgCtx.chatId());
                    }
                })
                .reply(update -> {
                    sendCallbackResponse(update.getCallbackQuery().getId());
                    sendMovie(update.getCallbackQuery().getData().replace(SEARCH_REPLY, ""),
                            update.getCallbackQuery().getMessage().getChatId());
                }, CALLBACK_QUERY, isStartWith(SEARCH_REPLY))
                .build();
    }

    private List<String> getCallbackMessage(String data){
        List<String> result = Arrays.asList(data.split("#"));
        return result.subList(1, result.size());
    }

    public Ability commandOpen(){
        return Ability
                .builder()
                .name("open")
                .info("Open movie by id")
                .locality(ALL)
                .privacy(PUBLIC)
                .action(msg -> openMovie(Arrays.stream(msg.arguments()).reduce((s, s2) -> s + " " + s2).orElse("")))
                .reply(update -> {
                    sendCallbackResponse(update.getCallbackQuery().getId());
                    String data = update.getCallbackQuery().getData().replace(OPEN_REPLY,"");
                    List<Button> buttons = messageStateService.getStateByPath(
                            data,
                            update.getCallbackQuery().getMessage().getChatId(),
                            update.getCallbackQuery().getMessage().getMessageId());
                    buttons.add(createFavoriteButton(TelegramClient.of(update.getCallbackQuery().getFrom().getId()), data.split("#")[0]));
                    EditMessageReplyMarkup msg = createReplyButtons(buttons, OPEN_REPLY);
                    msg.setChatId(update.getCallbackQuery().getMessage().getChatId());
                    msg.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
                    silent.executeAsync(msg);
                }, CALLBACK_QUERY, isStartWith(OPEN_REPLY))
                .build();
    }

    private Consumer<MessageContext> starterHandler() {
        return msgCtx -> {
            userService.registerUser(msgCtx.user().firstName(), msgCtx.user().username(), TelegramClient.of(msgCtx.user().id()));
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

    private void sendMovie(String movieId, Long chatId) {
        SendPhoto msg = openMovie(movieId);
        msg.setChatId(chatId);
        try {
            sendPhoto(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private Button createFavoriteButton(TelegramClient client, String movieId){
        boolean isFavorite = userService.checkFavorite(client, Integer.valueOf(movieId));
        if(isFavorite)
            return Button.favoriteButton(movieId, false);
        else
            return Button.favoriteButton(movieId, true);
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
            inlineBtn.setCallbackData((btn.getMenuType() == null ? menuType : btn.getMenuType()) + btn.getData());
            return inlineBtn;
        }).collect(Collectors.toList());

        List<InlineKeyboardButton> responseInlineButtons = responseButtons.stream()
                .sorted(Comparator.nullsLast(Comparator.comparing(Button::getOrder)))
                .map(button -> {
            InlineKeyboardButton inlineButton = new InlineKeyboardButton();
            inlineButton.setText(button.getName());
            if(button.getData() != null)
                inlineButton.setCallbackData(menuType + button.getData());
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
                  .setCallbackData(SEARCH_REPLY + searchItem.getId());
        }
        message.setChatId(chatId);
        silent.executeAsync(message);
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
