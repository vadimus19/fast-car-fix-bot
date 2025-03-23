package com.example.fast_car_fix_bot.controller;

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
import java.util.List;

@Slf4j
@Component
public class CarRepairBot extends TelegramLongPollingBot { // для этого класса нужен отдельный пакет, к контроллерам его лучше не относить
    private final RepairService repairService; // это поле не используется
    private final ServiceCenterRepository serviceCenterRepository;

    @Value("${telegram.bot.token}")
    private String botToken; // это поле не используется

    @Value("${telegram.bot.username}")
    private String botUsername; // это поле не используется

    // у тебя не используются
    private final java.util.Map<Long, Step> userSteps = new java.util.HashMap<>(); // java.util. - этот префикс всегда убирай и добавляй импорт
    private final java.util.Map<Long, String> userDescriptions = new java.util.HashMap<>();
    private final java.util.Map<Long, String> userProblems = new java.util.HashMap<>();

    @Autowired
    // @Lazy - скопировал откуда-то или специально поставил эту аннотацию?
    public CarRepairBot(@Lazy RepairService repairService, ServiceCenterRepository serviceCenterRepository) {
        this.repairService = repairService;
        this.serviceCenterRepository = serviceCenterRepository;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    } // getters/setters не обязательно всегда создать. Только если тебе необходимо где-то название бота

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onRegister() {
        super.onRegister();
        log.info("✅ Bot {} successfully registered with the Telegram API!", botUsername); // ✅ - это красиво, но такие знаки лучше не использовать. Т.к. в разной кодировке могут быть неожиданностей
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update != null && update.getMessage() != null) {
            Message message = update.getMessage();
            Long chatId = message.getChatId();
            String text = message.getText();

            if (text != null && !text.trim().isEmpty()) { // можно заменить на StringUtils.isNotEmpty()
                if ("/start".equals(text)) {
                    sendTextMessage(chatId, "Welcome! Please share your location to continue.");
                } else if ("back".equals(text)) {
                    sendTextMessage(chatId, "Please choose an issue using the buttons below.");
                    sendInlineKeyboard(chatId);  // Отправка inline клавиатуры
                } else if ("Submit Request".equals(text)) {
                    sendTextMessage(chatId, "Your request has been submitted!");
                } else {
                    sendTextMessage(chatId, "Sorry, I didn't understand that.");
                }
            } else {
                sendTextMessage(chatId, "❌ The message is empty or not recognized. Please send a valid command or text.");
            }
        }

        /* попробуй эту конструкцию заменить на
        Optional.ofNullable(update)
        .filter(item -> StringUtils.isNotEmpty(update.getMessage() ) )
        .filter(item -> StringUtils.isNotEmpty(update.getMessage().getLocation() ) )
        .ifPresent(item -> здесь вызов метода, который выполнит логику)
        */
        if (update != null && update.getMessage() != null && update.getMessage().getLocation() != null) {
            double latitude = update.getMessage().getLocation().getLatitude();
            double longitude = update.getMessage().getLocation().getLongitude();
            if (latitude != 0.0 && longitude != 0.0) { // магические числа в константы класса
                sendTextMessage(update.getMessage().getChatId(), "Your location: " + latitude + ", " + longitude);
                List<ServiceCenter> nearbyCenters = serviceCenterRepository.findNearby(latitude, longitude);
                if (!nearbyCenters.isEmpty()) {
                    StringBuilder sb = new StringBuilder("Nearby service centers:\n"); // sb - лучше переименовать по содержимому переменной
                    for (ServiceCenter center : nearbyCenters) { // попробуй здесь использовать stream() API у коллекций, а в конце получи нужную строку
                        sb.append(center.getName()).append(" - ").append(center.getAddress()).append("\n");
                    }
                    sendTextMessage(update.getMessage().getChatId(), sb.toString());
                } else {
                    sendTextMessage(update.getMessage().getChatId(), "No nearby service centers found.");
                }
            } else {
                sendTextMessage(update.getMessage().getChatId(), "❌ The location data is not valid. Please send your location again."); // ❌ - красиво, но технически лучше не использовать
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

    public void sendLocationButton(Long chatId) { // метод пока не используется. *Button в конце лучше убрать
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
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
        InlineKeyboardButton button1 = new InlineKeyboardButton("Oil Change"); // OilChange в константу
        button1.setCallbackData("Oil Change"); // button1 - changeOilButton

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