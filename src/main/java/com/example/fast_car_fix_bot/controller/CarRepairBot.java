package com.example.fast_car_fix_bot.controller;

import com.example.fast_car_fix_bot.entity.RepairRequest;
import com.example.fast_car_fix_bot.repository.RepairService;
import com.example.fast_car_fix_bot.repository.ServiceCenterRepository;
import com.example.fast_car_fix_bot.service.ServiceCenter;
import com.example.fast_car_fix_bot.service.Step;
import io.micrometer.common.util.StringUtils;
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

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CarRepairBot extends TelegramLongPollingBot { // для этого класса нужен отдельный пакет, к контроллерам его лучше не относить

    private final ServiceCenterRepository serviceCenterRepository;
    private final RepairService repairService;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    private static final String OIL_CHANGE = "Oil Change";
    private static final String TIRE_CHANGE = "Tire Change";
    private static final String ELECTRICIAN = "Electrician";
    private static final String GLASS_REPLACEMENT = "Glass Replacement";
    private static final String CHASSIS_DIAGNOSTICS = "Chassis Diagnostics";
    private static final String ENGINE_DIAGNOSTICS = "Engine Diagnostics";
    private static final String TOWING = "Towing";

    private Map<Long, String> problemDescriptions = new HashMap<>(); // For storing problem descriptions by chatId

    @Autowired
    public CarRepairBot(ServiceCenterRepository serviceCenterRepository, @Lazy RepairService repairService) {
        this.serviceCenterRepository = serviceCenterRepository;
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
        log.info("Bot {} successfully registered with the Telegram API!", botUsername);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update == null) {
            return;
        }

        // Обработка текстовых сообщений
        if (update.getMessage() != null && update.getMessage().getText() != null) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            if (StringUtils.isNotEmpty(text)) {
                // Handle commands using a switch statement
                switch (text) {
                    case "/start":
                        sendTextMessage(chatId, "Welcome! Please choose a problem from the options below.");
                        sendInlineKeyboard(chatId); // Send inline keyboard
                        break;
                    case "back":
                        sendTextMessage(chatId, "Please choose an issue using the buttons below.");
                        sendInlineKeyboard(chatId); // Send inline keyboard
                        break;
                    case "Submit Request":
                        sendTextMessage(chatId, "Your request has been submitted!");
                        break;
                    default:
                        sendTextMessage(chatId, "Sorry, I didn't understand that.");
                        break;
                }
            } else {
                sendTextMessage(chatId, "❌ The message is empty or not recognized. Please send a valid command or text.");
            }
        }

        // Обработка callback запросов (кнопки)
        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();

            // Handle problem selection through buttons
            switch (callbackData) {
                case OIL_CHANGE:
                case TIRE_CHANGE:
                case ELECTRICIAN:
                case GLASS_REPLACEMENT:
                case CHASSIS_DIAGNOSTICS:
                case ENGINE_DIAGNOSTICS:
                case TOWING:
                    sendTextMessage(chatId, "You have selected '" + callbackData + "'. Please describe the issue.");
                    problemDescriptions.put(chatId, callbackData); // Save selected problem
                    break;
                case "back":
                    sendTextMessage(chatId, "Going back.");
                    sendInlineKeyboard(chatId); // Send inline keyboard
                    break;
                case "submit":
                    sendTextMessage(chatId, "Your request has been submitted!");
                    break;
                default:
                    sendTextMessage(chatId, "Sorry, I didn't understand that.");
                    break;
            }
        }

        // Обработка текстовых сообщений с описанием проблемы
        if (update.getMessage() != null && update.getMessage().getText() != null && !update.getMessage().getText().isEmpty()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            // If this is a problem description, ask to send location
            if (problemDescriptions.containsKey(chatId) && messageText.length() > 0) {
                sendTextMessage(chatId, "Description received! Now please send your location.");
                sendLocationButton(chatId); // Show button for location
            }
        }

        // Обработка сообщений с локацией
        Optional.ofNullable(update)
                .map(Update::getMessage)
                .map(Message::getLocation)
                .filter(location -> location.getLatitude() != 0.0 && location.getLongitude() != 0.0)
                .ifPresent(location -> {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    sendTextMessage(update.getMessage().getChatId(), "Your location: " + latitude + ", " + longitude);
                    List<ServiceCenter> nearbyCenters = serviceCenterRepository.findNearby(latitude, longitude);
                    if (!nearbyCenters.isEmpty()) {
                        String nearbyCentersInfo = nearbyCenters.stream()
                                .map(center -> center.getName() + " - " + center.getAddress())
                                .collect(Collectors.joining("\n", "Nearby service centers:\n", ""));
                        sendTextMessage(update.getMessage().getChatId(), nearbyCentersInfo);
                    } else {
                        sendTextMessage(update.getMessage().getChatId(), "No nearby service centers found.");
                    }

                    // After sending location, show "Submit Request" button
                    sendSubmitButton(update.getMessage().getChatId());
                });
    }

    // Method to send location button
    public void sendLocationButton(Long chatId) {
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

    // Method to send "Submit Request" button
    public void sendSubmitButton(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Would you like to submit your request?");

        InlineKeyboardButton submitButton = new InlineKeyboardButton("Submit Request");
        submitButton.setCallbackData("submit");

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(submitButton);

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(row);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(keyboard);

        message.setReplyMarkup(inlineKeyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending submit button: ", e);
        }
    }

    // Method to send Inline keyboard
    public void sendInlineKeyboard(Long chatId) {
        InlineKeyboardButton button1 = new InlineKeyboardButton(OIL_CHANGE);
        button1.setCallbackData(OIL_CHANGE);

        InlineKeyboardButton button2 = new InlineKeyboardButton(TIRE_CHANGE);
        button2.setCallbackData(TIRE_CHANGE);

        InlineKeyboardButton button3 = new InlineKeyboardButton(ELECTRICIAN);
        button3.setCallbackData(ELECTRICIAN);

        InlineKeyboardButton button4 = new InlineKeyboardButton(GLASS_REPLACEMENT);
        button4.setCallbackData(GLASS_REPLACEMENT);

        InlineKeyboardButton button5 = new InlineKeyboardButton(CHASSIS_DIAGNOSTICS);
        button5.setCallbackData(CHASSIS_DIAGNOSTICS);

        InlineKeyboardButton button6 = new InlineKeyboardButton(ENGINE_DIAGNOSTICS);
        button6.setCallbackData(ENGINE_DIAGNOSTICS);

        InlineKeyboardButton button7 = new InlineKeyboardButton(TOWING);
        button7.setCallbackData(TOWING);

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

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(keyboard);

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Please choose the issue you want to report:");
        message.setReplyMarkup(inlineKeyboardMarkup);

        try {
            execute(message);  // Send the message with the keyboard
        } catch (TelegramApiException e) {
            log.error("Error sending inline keyboard: ", e);
        }
    }

    // Method to send text messages
    public void sendTextMessage(Long chatId, String messageText) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(messageText);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending message: ", e);
        }
    }}
