package org.bots.clients.telegram;

import org.bots.configuration.ApplicationVariables;
import org.bots.model.items.MovieSearchResponse;
import org.bots.screens.SearchScreen;
import org.bots.sources.SearchService;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.telegram.abilitybots.api.objects.Flag.CALLBACK_QUERY;
import static org.telegram.abilitybots.api.objects.Locality.ALL;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;

@Component
public class TelegramBot extends AbilityBot {

    private final SearchService searchService;
    private Map<String, Function> replyActionMap = new HashMap<>();

    private static final String OPEN_ACTION = "OPEN#";

    public TelegramBot(SearchService searchService) {
        super(ApplicationVariables.TELEGRAM_TOKEN, ApplicationVariables.BOT_NAME);
        this.searchService = searchService;
    }


    @Override
    public int creatorId() {
        return 291863262;
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

    public Ability getSearch(){
        return Ability
                .builder()
                .name("search")
                .info("Search movie")
                .locality(ALL)
                .privacy(PUBLIC)
                .action(searchHandler())
                .build();
    }

    private Consumer<MessageContext> searchHandler() {
        return msgCtx -> {
            SendMessage msg = searchMovie(Arrays.stream(msgCtx.arguments()).reduce((s, s2) -> s + " " + s2).orElse(""));
            msg.setChatId(msgCtx.chatId());
            silent.execute(msg);

        };
    }

    private Ability handleReply(){
        return Ability
                .builder()
                .reply(searchReplyHandler(), CALLBACK_QUERY)
                .build();
    }

    private Consumer<Update> searchReplyHandler(){
        return update -> {
            AnswerCallbackQuery answer = new AnswerCallbackQuery();
            answer.setCallbackQueryId(update.getCallbackQuery().getId());
            silent.executeAsync(answer);

            processReply(update.getCallbackQuery());
        };
    }

    private void processReply(CallbackQuery callbackQuery) {
        SendMessage message = null;
        String command = callbackQuery.getData().substring(0, callbackQuery.getData().indexOf("#"));
        switch (command) {
            case OPEN_ACTION: message = openMovie(command); break;
            default: break;
        }
        if(message != null)
            silent.executeAsync(message);
    }


    private SendMessage searchMovie(String param){
        SendMessage message = new SendMessage();


        SearchScreen searchScreen = searchService.searchMovie(param);
        StringBuilder text = new StringBuilder();
        int rank = 1;
        for(MovieSearchResponse movie : searchScreen.getMovieList()){
            if(text.length() != 0)
                text.append("------------------------------------------").append("\n");
                text.append("_").append(rank++).append("_ ")
                    .append("*Title:*").append(movie.getTitle()).append("\n")
                    .append("\t*Year:*").append(movie.getYear()).append("\n")
                    .append("\t*Original*").append(movie.getOriginalName()).append("\n");
        }

        message.setText(text.toString());
        message.setParseMode(ParseMode.MARKDOWN);

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        message.setReplyMarkup(keyboard);
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        keyboard.setKeyboard(Collections.singletonList(buttons));
        rank = 1;
        for(String screenButton: searchScreen.getButtons() ) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            buttons.add(button);
            button.setText(String.valueOf(rank++))
                  .setCallbackData(OPEN_ACTION + screenButton);
        }

        return message;
    }

    private SendMessage openMovie(String movieId){
        return null;
    }


}
