package com.example.fast_car_fix_bot.service;

import com.example.fast_car_fix_bot.bot.BotFlowService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class MessageHandler {

    private final BotFlowService flow;

    public MessageHandler(BotFlowService flow) {
        this.flow = flow;
    }

    public void handle(Message message) {
        flow.handleMessage(message);
    }

    public void handleCallback(CallbackQuery callback) {
        flow.handleCallback(callback);
    }
}