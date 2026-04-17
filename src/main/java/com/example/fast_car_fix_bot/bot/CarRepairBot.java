package com.example.fast_car_fix_bot.bot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class CarRepairBot extends TelegramLongPollingBot {

    private final UpdateDispatcher dispatcher;

    @Value("${telegram.bot.username}")
    private String username;

    @Value("${telegram.bot.token}")
    private String token;

    public CarRepairBot(UpdateDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage()) {
            dispatcher.dispatch(update);
        }

        if (update.hasCallbackQuery()) {
            dispatcher.dispatchCallback(update.getCallbackQuery());
        }
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }
}