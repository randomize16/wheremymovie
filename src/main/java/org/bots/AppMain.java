package org.bots;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.ApiContextInitializer;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class AppMain  {

    public static void main(String[] args) {
        ApiContextInitializer.init();
        SpringApplication.run(AppMain.class, args);
    }
}
