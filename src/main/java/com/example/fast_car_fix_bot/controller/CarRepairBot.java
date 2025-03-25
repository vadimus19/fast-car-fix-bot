package com.example.fast_car_fix_bot.controller;

import com.example.fast_car_fix_bot.entity.RepairRequest;
import com.example.fast_car_fix_bot.repository.RepairService;
import com.example.fast_car_fix_bot.repository.ServiceCenterRepository;
import com.example.fast_car_fix_bot.service.ServiceCenter;
import com.example.fast_car_fix_bot.service.Step;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Component
public class CarRepairBot extends TelegramLongPollingBot {
    private final RepairService repairService;
    private final ServiceCenterRepository serviceCenterRepository;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    private final java.util.Map<Long, Step> userSteps = new HashMap<>();
    private final java.util.Map<Long, String> userDescriptions = new HashMap<>();
    private final java.util.Map<Long, String> userProblems = new HashMap<>();

    @Autowired
    public CarRepairBot(@Lazy RepairService repairService, ServiceCenterRepository serviceCenterRepository) {
        this.repairService = repairService;
        this.serviceCenterRepository = serviceCenterRepository;
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
        // Проверяем, что обновление содержит сообщение
        if (update != null && update.getMessage() != null) {
            // Извлекаем chatId из сообщения
            Long chatId = update.getMessage().getChatId();
            String text = update.getMessage().getText();

            // Проверяем, что текст сообщения не пустой
            if (text != null && !text.trim().isEmpty()) {
                if ("/start".equals(text)) {
                    sendTextMessage(String.valueOf(chatId), "Welcome! Please share your location to continue.");
                } else if ("back".equals(text)) {
                    sendTextMessage(String.valueOf(chatId), "Please choose an issue using the buttons below.");
                    sendInlineKeyboard(String.valueOf(chatId));  // Отправка inline клавиатуры
                } else if ("Submit Request".equals(text)) {
                    sendTextMessage(String.valueOf(chatId), "Your request has been submitted!");
                } else {
                    sendTextMessage(String.valueOf(chatId), "Sorry, I didn't understand that.");
                }
            } else {
                sendTextMessage(String.valueOf(chatId), "❌ The message is empty or not recognized. Please send a valid command or text.");
            }

            // Обработка локации
            if (update.getMessage().getLocation() != null) {
                double latitude = update.getMessage().getLocation().getLatitude();
                double longitude = update.getMessage().getLocation().getLongitude();

                if (latitude != 0.0 && longitude != 0.0) {
                    log.info("Received location: Latitude = {}, Longitude = {}", latitude, longitude);
                    sendTextMessage(String.valueOf(chatId), "Your location: " + latitude + ", " + longitude);

                    try {
                        // Ищем ближайшие сервисные центры
                        List<ServiceCenter> nearbyCenters = serviceCenterRepository.findNearby(latitude, longitude);
                        if (!nearbyCenters.isEmpty()) {
                            StringBuilder sb = new StringBuilder("Nearby service centers:\n");
                            for (ServiceCenter center : nearbyCenters) {
                                sb.append(center.getName()).append(" - ").append(center.getAddress()).append("\n");
                            }
                            sendTextMessage(String.valueOf(chatId), sb.toString());
                        } else {
                            sendTextMessage(String.valueOf(chatId), "No nearby service centers found.");
                            sendTextMessage(String.valueOf(chatId), "Please enter your city manually.");
                            userSteps.put(Long.valueOf(String.valueOf(chatId)), Step.CHOOSING_CITY); // Сохраняем шаг
                        }
                    } catch (Exception e) {
                        log.error("Error fetching nearby service centers: ", e);
                        sendTextMessage(String.valueOf(chatId), "❌ An error occurred while fetching nearby service centers. Please try again later.");
                        sendTextMessage(String.valueOf(chatId), "Please enter your city manually.");
                        userSteps.put(Long.valueOf(String.valueOf(chatId)), Step.CHOOSING_CITY);
                    }
                } else {
                    sendTextMessage(String.valueOf(chatId), "❌ The location data is not valid. Please send your location again.");
                }
            }

            // Если пользователь вводит город вручную
            // Если пользователь вводит город вручную
            if (userSteps.containsKey(String.valueOf(chatId)) && userSteps.get(String.valueOf(chatId)) == Step.CHOOSING_CITY) {
                String city = update.getMessage().getText();
                log.info("User entered city: {}", city);

                // В данном случае принимаем любой город
                sendTextMessage(String.valueOf(chatId), "City: " + city + " received. Please select a problem:");

                // Переходим к выбору проблемы
                sendInlineKeyboard(String.valueOf(chatId));
                userSteps.put(Long.valueOf(String.valueOf(chatId)), Step.CHOOSING_PROBLEM); // Переход к следующему шагу
            }

            // Если пользователь выбирает проблему и вводит описание
            if (userSteps.containsKey(String.valueOf(chatId)) && userSteps.get(String.valueOf(chatId)) == Step.CHOOSING_PROBLEM) {
                String selectedProblem = update.getMessage().getText();
                userProblems.put(Long.valueOf(String.valueOf(chatId)), selectedProblem); // Сохраняем выбранную проблему

                sendTextMessage(String.valueOf(chatId), "You selected: " + selectedProblem + ". Please provide a description.");
                userSteps.put(Long.valueOf(String.valueOf(chatId)), Step.TYPING_DESCRIPTION); // Переход к следующему шагу
            }

            // Если пользователь вводит описание проблемы
            if (userSteps.containsKey(String.valueOf(chatId)) && userSteps.get(String.valueOf(chatId)) == Step.TYPING_DESCRIPTION) {
                String description = update.getMessage().getText();
                userDescriptions.put(Long.valueOf(String.valueOf(chatId)), description); // Сохраняем описание

                sendTextMessage(String.valueOf(chatId), "Your description: " + description + ". Please wait while your request is processed.");

                // Отправка на обработку запроса
                repairService.processMessage(chatId, description); // Отправка запроса на обработку
                userSteps.put(Long.valueOf(String.valueOf(chatId)), Step.REQUEST_SUBMITTED); // Переход к этапу "Запрос отправлен"
            }
        }
    }


    public void sendTextMessage(String chatId, String messageText) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageText);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending message in Telegram: ", e);
        }
    }

    public void sendLocationButton(String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Please share your location:");

        KeyboardButton locationButton = new KeyboardButton("Send Location");
        locationButton.setRequestLocation(true);

        KeyboardRow row = new KeyboardRow();
        row.add(locationButton);

        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(row);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending location button in Telegram: ", e);
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

    private void sendInlineKeyboard(String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Please choose one of the issues:");

        message.setReplyMarkup(createInlineKeyboard());

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending keyboard in Telegram: ", e);
        }
    }
}