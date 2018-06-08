package org.bots.model.items;

import static org.bots.clients.telegram.TelegramBot.FAVORITE_COMMAND;
import static org.bots.clients.telegram.TelegramBot.OPEN_COMMAND;

public class Button {
    private String name;
    private String data;
    private String url;
    private Integer order = 0;
    private ButtonType type;
    private String menuType;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ButtonType getType() {
        return type;
    }

    public void setType(ButtonType type) {
        this.type = type;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getMenuType() {
        return menuType;
    }

    public void setMenuType(String menuType) {
        this.menuType = menuType;
    }

    public enum ButtonType {
        RESPONSE, MENU
    }

    public static Button backButton(String data) {
        Button btn = new Button();
        btn.setOrder(0);
        btn.setType(ButtonType.MENU);
        btn.setData(data);
        btn.setName("⬅");
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
            btn.setName("⭐");
        else{
            btn.setName("⛔");
        }
        return btn;
    }
}
