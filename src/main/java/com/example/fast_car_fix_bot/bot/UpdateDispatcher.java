package com.example.fast_car_fix_bot.bot;

import com.example.fast_car_fix_bot.service.MessageHandler;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class UpdateDispatcher {

    private final MessageHandler handler;

    public UpdateDispatcher(MessageHandler handler) {
        this.handler = handler;
    }

    public void dispatch(Update update) {
        handler.handle(update.getMessage());
    }

    public void dispatchCallback(CallbackQuery callback) {
        handler.handleCallback(callback);
    }
}