package com.example.fast_car_fix_bot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.io.Serializable;

@Slf4j
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

    public void send(SendMessage message) {
        try {
            sender.execute(message);
        } catch (Exception e) {
            log.error("Failed to send message", e);
        }
    }

    public <T extends Serializable, Method extends BotApiMethod<T>> T execute(Method method) {
        try {
            return sender.execute(method);
        } catch (Exception e) {
            log.error("Telegram API error", e);
            return null;
        }
    }
}