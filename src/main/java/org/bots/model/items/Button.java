package org.bots.model.items;

import lombok.Data;

import static org.bots.clients.telegram.TelegramBot.FAVORITE_COMMAND;
import static org.bots.clients.telegram.TelegramBot.OPEN_COMMAND;
import static org.bots.clients.telegram.TelegramBot.SUBSCRIBE_COMMAND;

@Data
public class Button {
    private String name;
    private String data;
    private String url;
    private Integer order = 0;
    private ButtonType type;
    private String menuType;

    public enum ButtonType {
        RESPONSE, MENU
    }

    public static Button backButton(String data) {
        Button btn = new Button();
        btn.setOrder(-1);
        btn.setType(ButtonType.RESPONSE);
        btn.setData(data);
        btn.setName("⬅ Back");
        btn.setMenuType(OPEN_COMMAND);
        return btn;
    }

    public static Button favoriteButton(String movieId, boolean isFavorite) {
        Button btn = new Button();
        btn.setOrder(0);
        btn.setType(ButtonType.MENU);
        btn.setMenuType(FAVORITE_COMMAND);
        btn.setData(movieId);
        btn.setMenuType(FAVORITE_COMMAND);
        if(!isFavorite)
            btn.setName("⭐ Favorite");
        else{
            btn.setName("⛔ Favorite");
        }
        return btn;
    }

    public static Button subscribeButton(String movieId, boolean isSubscribed) {
        Button btn = new Button();
        btn.setOrder(1);
        btn.setType(ButtonType.MENU);
        btn.setMenuType(SUBSCRIBE_COMMAND);
        btn.setData(movieId);
        btn.setMenuType(SUBSCRIBE_COMMAND);
        if(!isSubscribed)
            btn.setName("⏰ Subscribe");
        else{
            btn.setName("☠ Subscribe");
        }
        return btn;
    }
}
