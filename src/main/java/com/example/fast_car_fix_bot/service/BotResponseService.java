package com.example.fast_car_fix_bot.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

@Service
public class BotResponseService {

    private final TelegramSender sender;

    public BotResponseService(TelegramSender sender) {
        this.sender = sender;
    }

    public void send(Long userId, String text) {
        SendMessage msg = new SendMessage();
        msg.setChatId(userId.toString());
        msg.setText(text);

        sender.send(msg);
    }

    public void sendWithButtons(Long userId,
                                String text,
                                List<List<InlineKeyboardButton>> keyboard) {

        SendMessage msg = new SendMessage();
        msg.setChatId(userId.toString());
        msg.setText(text);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);

        msg.setReplyMarkup(markup);

        sender.send(msg);
    }

    public void sendLocationRequest(Long userId, String text) {

        SendMessage msg = new SendMessage();
        msg.setChatId(userId.toString());
        msg.setText(text);

        KeyboardButton locationButton = new KeyboardButton("📍 Send location");
        locationButton.setRequestLocation(true);

        KeyboardRow row = new KeyboardRow();
        row.add(locationButton);

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setKeyboard(List.of(row));
        markup.setResizeKeyboard(true);
        markup.setOneTimeKeyboard(true);

        msg.setReplyMarkup(markup);

        sender.send(msg);
    }

    public void answerCallback(String callbackId) {
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(callbackId);

        sender.execute(answer);
    }
}