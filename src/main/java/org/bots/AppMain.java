package org.bots;

import org.bots.sources.FilmixSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.generics.LongPollingBot;

import java.util.List;
import java.util.logging.Logger;

@SpringBootApplication
public class AppMain implements CommandLineRunner {

    private static Logger log = Logger.getLogger(AppMain.class.getSimpleName());

    @Autowired
    TelegramBotsApi telegramBotsApi;

    @Autowired
    List<LongPollingBot> bots;

    @Autowired
    FilmixSource filmixSource;

    public static void main(String[] args) {
        ApiContextInitializer.init();
        SpringApplication.run(AppMain.class, args);
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
