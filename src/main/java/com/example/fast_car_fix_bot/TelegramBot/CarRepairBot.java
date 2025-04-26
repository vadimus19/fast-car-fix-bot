package com.example.fast_car_fix_bot.TelegramBot;

import com.example.fast_car_fix_bot.repository.RepairService;
import com.example.fast_car_fix_bot.repository.ServiceCenterRepository;
import com.example.fast_car_fix_bot.service.RepairServiceType;
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

@Slf4j
@Component
public class CarRepairBot extends TelegramLongPollingBot {

    private final ServiceCenterRepository serviceCenterRepository;
    private final RepairService repairService;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    private Map<Long, RepairServiceType> problemDescriptions = new HashMap<>();
    private Map<Long, Step> userSteps = new HashMap<>();
    private Map<Long, Double> latitudeMap = new HashMap<>();
    private Map<Long, Double> longitudeMap = new HashMap<>();

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
        Long chatId = update.getMessage() != null ? update.getMessage().getChatId() : update.getCallbackQuery().getMessage().getChatId();

        if (update.getMessage() != null && update.getMessage().getText() != null) {
            String text = update.getMessage().getText();
            if (StringUtils.isNotEmpty(text)) {
                switch (text) {
                    case "/start":
                        sendTextMessage(chatId, "Welcome! Please select a problem from the options below.");
                        sendInlineKeyboard(chatId);
                        userSteps.put(chatId, Step.SELECTING_PROBLEM);
                        break;
                    case "back":
                        sendTextMessage(chatId, "Please select a problem using the buttons below.");
                        sendInlineKeyboard(chatId);
                        userSteps.put(chatId, Step.SELECTING_PROBLEM);
                        break;
                    case "Submit Request":
                        sendTextMessage(chatId, "Your request has been submitted!");
                        break;
                    default:
                        sendTextMessage(chatId, "Sorry, I didn't understand your command.");
                        break;
                }
            } else {
                sendTextMessage(chatId, "❌ The message is empty or unrecognized. Please send a command or text.");
            }
        }

        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            try {
                RepairServiceType selectedService = RepairServiceType.valueOf(callbackData);
                problemDescriptions.put(chatId, selectedService);
                sendTextMessage(chatId, "You selected '" + selectedService.getDescription() + "'. Please describe the problem.");
                userSteps.put(chatId, Step.DESCRIBING_ISSUE);
            } catch (IllegalArgumentException e) {
                sendTextMessage(chatId, "Sorry, I didn't understand that selection.");
            }
        }

        if (update.getMessage() != null && update.getMessage().getText() != null && !update.getMessage().getText().isEmpty()) {
            String messageText = update.getMessage().getText();
            if (userSteps.get(chatId) == Step.DESCRIBING_ISSUE && messageText.length() > 0) {
                sendTextMessage(chatId, "Description received! Now, please send your location.");
                sendLocationButton(chatId);
                userSteps.put(chatId, Step.SHARING_LOCATION);
            } else if (userSteps.get(chatId) == Step.DESCRIBING_ISSUE) {
                sendTextMessage(chatId, "Please provide a description of the problem.");
            }
        }

        Optional.ofNullable(update)
                .map(Update::getMessage)
                .map(Message::getLocation)
                .filter(location -> location.getLatitude() != 0.0 && location.getLongitude() != 0.0)
                .ifPresent(location -> {
                    sendTextMessage(chatId, "Your location: " + location.getLatitude() + ", " + location.getLongitude());
                    latitudeMap.put(chatId, location.getLatitude());
                    longitudeMap.put(chatId, location.getLongitude());
                    sendSubmitButton(chatId);
                    userSteps.put(chatId, Step.SUBMITTING_REQUEST);
                    String description = problemDescriptions.get(chatId).getDescription();
                    double latitude = latitudeMap.get(chatId);
                    double longitude = longitudeMap.get(chatId);

                    repairService.processMessage(chatId, description, latitude, longitude);
                });
    }

    public void sendLocationButton(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Please send your location:");

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
            log.error("Error sending location button: ", e);
        }
    }

    public void sendSubmitButton(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Do you want to submit your request?");

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
            log.error("Error sending submit request button: ", e);
        }
    }

    public void sendInlineKeyboard(Long chatId) {
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        for (RepairServiceType service : RepairServiceType.values()) {
            InlineKeyboardButton button = new InlineKeyboardButton(service.getDescription());
            button.setCallbackData(service.name());
            buttons.add(button);
        }

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        for (int i = 0; i < buttons.size(); i += 2) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(buttons.get(i));
            if (i + 1 < buttons.size()) {
                row.add(buttons.get(i + 1));
            }
            keyboard.add(row);
        }

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(keyboard);

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Please select a problem:");

        message.setReplyMarkup(inlineKeyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending inline keyboard: ", e);
        }
    }

    public void sendTextMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending text message: ", e);
        }
    }
}