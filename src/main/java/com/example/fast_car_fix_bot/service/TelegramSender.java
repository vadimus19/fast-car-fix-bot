package com.example.fast_car_fix_bot.service;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.io.Serializable;

@Service
public class TelegramSender {

    private final AbsSender sender;

    public TelegramSender(@Value("${telegram.bot.token}") String token) {

        this.sender = new DefaultAbsSender(new DefaultBotOptions()) {
            @Override
            public String getBotToken() {
                return token;
            }
        };
    }

    @SneakyThrows
    public void send(SendMessage message) {
        sender.execute(message);
    }

    @SneakyThrows
    public <T extends Serializable, Method extends BotApiMethod<T>> T execute(Method method) {
        return sender.execute(method);
    }
}