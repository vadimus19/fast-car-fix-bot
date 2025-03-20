package com.example.fast_car_fix_bot.controller;

import com.example.fast_car_fix_bot.repository.RepairService;
import com.example.fast_car_fix_bot.service.Step;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class CarRepairBot extends TelegramLongPollingBot {
    private final RepairService repairService;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    private final java.util.Map<Long, Step> userSteps = new java.util.HashMap<>();
    private final java.util.Map<Long, String> userDescriptions = new java.util.HashMap<>();
    private final java.util.Map<Long, String> userProblems = new java.util.HashMap<>();

    public CarRepairBot(@Lazy RepairService repairService) {
        this.repairService = repairService;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onRegister() {
        super.onRegister();
        log.info("✅ Bot {} successfully registered with the Telegram API!", botUsername);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update != null && update.getMessage() != null) {
            Message message = update.getMessage();
            Long chatId = message.getChatId();
            String text = message.getText();

            if ("/start".equals(text)) {
                sendTextMessage(chatId, "Welcome! Please choose the issue you want to report.");
                sendInlineKeyboard(chatId);
                userSteps.put(chatId, Step.CHOOSING_PROBLEM);
            } else {
                Step step = userSteps.get(chatId);
                if (step == Step.TYPING_DESCRIPTION) {
                    userDescriptions.put(chatId, text);
                    sendTextMessage(chatId, "Your description has been saved. Click the 'Submit Request' button to submit.");
                    sendInlineKeyboard(chatId);
                    userSteps.put(chatId, Step.FINAL);
                }
            }
        }

        if (update != null && update.getCallbackQuery() != null) {
            String callbackData = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            log.info("Received callbackData: {}", callbackData);

            if ("back".equals(callbackData)) {
                sendTextMessage(chatId, "Please choose an issue using the buttons below.");
                sendInlineKeyboard(chatId);
                userSteps.put(chatId, Step.CHOOSING_PROBLEM);
            } else if ("submit".equals(callbackData)) {
                String problemDescription = userDescriptions.get(chatId);
                String problem = userProblems.get(chatId);

                if (problem == null || problem.isEmpty()) {
                    sendTextMessage(chatId, "Please choose an issue before submitting the request.");
                } else if (problemDescription == null || problemDescription.isEmpty()) {
                    sendTextMessage(chatId, "Please describe the problem before submitting the request.");
                } else {
                    repairService.processMessage(chatId, "Request: " + problem + ". Description: " + problemDescription);
                    sendTextMessage(chatId, "Request has been submitted. We will contact you shortly!");
                    userSteps.put(chatId, Step.FINAL);
                }
            } else {
                userProblems.put(chatId, callbackData);
                sendTextMessage(chatId, "You chose the issue: " + callbackData + ". Please provide a detailed description of the problem.");
                userSteps.put(chatId, Step.TYPING_DESCRIPTION);
            }
        }
    }

    public void sendTextMessage(Long chatId, String messageText) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(messageText);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending message in Telegram: ", e);
        }
    }

    public InlineKeyboardMarkup createInlineKeyboard() {
        InlineKeyboardButton button1 = new InlineKeyboardButton("Oil Change");
        button1.setCallbackData("Oil Change");

        InlineKeyboardButton button2 = new InlineKeyboardButton("Tire Change");
        button2.setCallbackData("Tire Change");

        InlineKeyboardButton button3 = new InlineKeyboardButton("Electrician");
        button3.setCallbackData("Electrician");

        InlineKeyboardButton button4 = new InlineKeyboardButton("Glass Replacement");
        button4.setCallbackData("Glass Replacement");

        InlineKeyboardButton button5 = new InlineKeyboardButton("Chassis Diagnostics");
        button5.setCallbackData("Chassis Diagnostics");

        InlineKeyboardButton button6 = new InlineKeyboardButton("Engine Diagnostics");
        button6.setCallbackData("Engine Diagnostics");

        InlineKeyboardButton button7 = new InlineKeyboardButton("Towing");
        button7.setCallbackData("Towing");

        InlineKeyboardButton backButton = new InlineKeyboardButton("Back");
        backButton.setCallbackData("back");

        InlineKeyboardButton submitButton = new InlineKeyboardButton("Submit Request");
        submitButton.setCallbackData("submit");

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(button1);
        row1.add(button2);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(button3);
        row2.add(button4);

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(button5);
        row3.add(button6);

        List<InlineKeyboardButton> row4 = new ArrayList<>();
        row4.add(button7);

        List<InlineKeyboardButton> row5 = new ArrayList<>();
        row5.add(backButton);
        row5.add(submitButton);

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);
        keyboard.add(row5);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(keyboard);

        return inlineKeyboardMarkup;
    }

    private void sendInlineKeyboard(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Please choose one of the issues:");

        message.setReplyMarkup(createInlineKeyboard());

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending keyboard in Telegram: ", e);
        }
    }
}