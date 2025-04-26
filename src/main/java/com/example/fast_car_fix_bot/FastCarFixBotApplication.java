package com.example.fast_car_fix_bot;

import com.example.fast_car_fix_bot.TelegramBot.CarRepairBot;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class FastCarFixBotApplication implements CommandLineRunner {

    private final CarRepairBot carRepairBot;

    public FastCarFixBotApplication(CarRepairBot carRepairBot) {
        this.carRepairBot = carRepairBot;
    }

    public static void main(String[] args) {
        SpringApplication.run(FastCarFixBotApplication.class, args);
    }

    @Override
    public void run(String... args) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(carRepairBot);
            System.out.println("Bot registered and listening for updates.");
        } catch (TelegramApiException e) {
            System.out.println("Error during bot registration");
            e.printStackTrace();
        }
    }
}