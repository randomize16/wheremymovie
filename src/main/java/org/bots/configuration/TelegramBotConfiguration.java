package org.bots.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.generics.LongPollingBot;

import java.util.List;

@Configuration
public class TelegramBotConfiguration implements CommandLineRunner {

    @Autowired
    private List<LongPollingBot> bots;

    @Autowired
    private TelegramBotsApi telegramBotsApi;

    @Bean
    public TelegramBotsApi telegramBotsApi(){
        return new TelegramBotsApi();
    }


    @Override
    public void run(String... args) throws Exception {
        if(bots != null){
            bots.forEach(bot -> {
                try {
                    telegramBotsApi.registerBot(bot);
                } catch (TelegramApiRequestException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
