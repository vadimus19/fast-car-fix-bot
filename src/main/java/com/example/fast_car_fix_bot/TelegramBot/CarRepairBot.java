package com.example.fast_car_fix_bot.TelegramBot;

import com.example.fast_car_fix_bot.entity.RepairRequest;
import com.example.fast_car_fix_bot.entity.ServiceCenter;
import com.example.fast_car_fix_bot.enums.RepairServiceType;
import com.example.fast_car_fix_bot.enums.Step;
import com.example.fast_car_fix_bot.exception.ResourceNotFoundException;
import com.example.fast_car_fix_bot.service.RepairService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class CarRepairBot extends TelegramLongPollingBot {

    private final RepairService repairService;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    public CarRepairBot(RepairService repairService) {
        this.repairService = repairService;
    }

    @Override
    public String getBotUsername() { return botUsername; }

    @Override
    public String getBotToken() { return botToken; }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
            return;
        }
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasText()) {
                if ("/start".equals(message.getText())) {
                    showMainMenu(message.getChatId());
                    return;
                }
                handleTextMessage(message);
                return;
            }
            if (message.hasLocation()) {
                handleLocationMessage(message);
            }
        }
    }

    private void handleCallback(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();
        String data = callbackQuery.getData();

        RepairRequest request = repairService.findActiveRequestByUser(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("No active request"));

        Step step = request.getCurrentStep();

        switch (step) {
            case SELECTING_PROBLEM -> handleProblemSelection(request, chatId, data);
            case CHOOSING_ACTION -> handleActionSelection(request, chatId, data);
            default -> sendTextMessage(chatId, "Please follow the instructions.");
        }
    }

    private void handleProblemSelection(RepairRequest request, Long chatId, String data) {
        try {
            RepairServiceType type = RepairServiceType.valueOf(data);
            request.setServiceType(type);
            request.setCurrentStep(Step.TYPING_DESCRIPTION);
            repairService.saveRepairRequest(request);
            sendTextMessage(chatId, "You selected: " + type.getDescription());
            sendTextMessage(chatId, "Please describe your problem:");
        } catch (IllegalArgumentException e) {
            sendTextMessage(chatId, "Unknown problem type selected.");
        }
    }

    private void handleActionSelection(RepairRequest request, Long chatId, String data) {
        request.setSelectedAction(data);

        switch (data) {
            case "Get Directions" -> {
                request.setCurrentStep(Step.SHARING_LOCATION);
                repairService.saveRepairRequest(request);
                sendLocationKeyboard(chatId);
            }
            case "Call Center" -> {
                request.setCurrentStep(Step.SUBMITTING_REQUEST);
                repairService.saveRepairRequest(request);
                sendTextMessage(chatId, "Our call center will contact you shortly.");
                showMainMenu(chatId);
            }
            default -> sendTextMessage(chatId, "Unknown action selected.");
        }
    }

    private void handleTextMessage(Message message) {
        Long chatId = message.getChatId();
        String text = message.getText();

        if ("New Request".equalsIgnoreCase(text)) {
            startNewRequest(chatId);
            return;
        }

        if ("Back".equalsIgnoreCase(text)) {
            showMainMenu(chatId);
            return;
        }

        RepairRequest request = repairService.findActiveRequestByUser(chatId).orElse(null);

        if (request == null || request.getCurrentStep() == null) {
            sendTextMessage(chatId, "Please start a new request using /start.");
            return;
        }

        Step step = request.getCurrentStep();

        if (step == Step.TYPING_DESCRIPTION) {
            if (text.isBlank()) {
                sendTextMessage(chatId, "Description cannot be empty.");
                return;
            }
            request.setDescription(text);
            request.setCurrentStep(Step.CHOOSING_ACTION);
            repairService.saveRepairRequest(request);
            showActionButtons(chatId);
        } else {
            sendTextMessage(chatId, "Please follow the instructions.");
        }
    }

    private void handleLocationMessage(Message message) {
        Long chatId = message.getChatId();
        RepairRequest request = repairService.findActiveRequestByUser(chatId).orElse(null);

        if (request == null || request.getCurrentStep() != Step.SHARING_LOCATION) {
            sendTextMessage(chatId, "Please select 'Get Directions' first.");
            return;
        }

        Location location = message.getLocation();
        request.setLatitude(location.getLatitude());
        request.setLongitude(location.getLongitude());
        request.setCurrentStep(Step.SHOWING_NEARBY);
        repairService.saveRepairRequest(request);

        sendTextMessage(chatId, "Searching nearby service centers...");
        showNearbyCenters(request);
    }

    private void showNearbyCenters(RepairRequest request) {
        Long chatId = request.getUserId();
        List<ServiceCenter> centers = repairService.findNearbyServiceCenters(
                request.getLatitude(), request.getLongitude());

        if (centers.isEmpty()) {
            sendTextMessage(chatId, "No nearby service centers found.");
            showMainMenu(chatId);
            return;
        }

        StringBuilder sb = new StringBuilder("Nearby service centers:\n");
        for (ServiceCenter c : centers) {
            sb.append(c.getName()).append(" - ").append(c.getAddress()).append("\n");
        }
        sendTextMessage(chatId, sb.toString());
        showMainMenu(chatId);
    }

    private void startNewRequest(Long chatId) {
        RepairRequest request = new RepairRequest();
        request.setUserId(chatId);
        request.setCurrentStep(Step.SELECTING_PROBLEM);
        repairService.saveRepairRequest(request);

        SendMessage msg = new SendMessage();
        msg.setChatId(String.valueOf(chatId));
        msg.setText("Choose the type of problem:");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (RepairServiceType type : RepairServiceType.values()) {
            InlineKeyboardButton btn = new InlineKeyboardButton();
            btn.setText(type.getDescription());
            btn.setCallbackData(type.name());
            rows.add(List.of(btn));
        }

        markup.setKeyboard(rows);
        msg.setReplyMarkup(markup);

        try { execute(msg); }
        catch (TelegramApiException e) { log.error("Error sending problem buttons", e); }
    }

    private void showActionButtons(Long chatId) {
        SendMessage msg = new SendMessage();
        msg.setChatId(String.valueOf(chatId));
        msg.setText("Select next action:");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardButton callBtn = new InlineKeyboardButton();
        callBtn.setText("Call Center");
        callBtn.setCallbackData("Call Center");

        InlineKeyboardButton directionsBtn = new InlineKeyboardButton();
        directionsBtn.setText("Get Directions");
        directionsBtn.setCallbackData("Get Directions");

        rows.add(List.of(callBtn, directionsBtn));
        markup.setKeyboard(rows);
        msg.setReplyMarkup(markup);

        try { execute(msg); }
        catch (TelegramApiException e) { log.error("Error sending action buttons", e); }
    }

    private void sendLocationKeyboard(Long chatId) {
        SendMessage msg = new SendMessage();
        msg.setChatId(String.valueOf(chatId));
        msg.setText("Please share your location:");

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(true);

        KeyboardButton locationButton = new KeyboardButton("Send location");
        locationButton.setRequestLocation(true);

        KeyboardRow row = new KeyboardRow();
        row.add(locationButton);
        keyboard.setKeyboard(List.of(row));

        msg.setReplyMarkup(keyboard);

        try { execute(msg); }
        catch (TelegramApiException e) { log.error("Error sending location keyboard", e); }
    }

    private void showMainMenu(Long chatId) {
        SendMessage msg = new SendMessage();
        msg.setChatId(String.valueOf(chatId));
        msg.setText("Choose an option:");

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(true);

        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton("New Request"));
        keyboard.setKeyboard(List.of(row));

        msg.setReplyMarkup(keyboard);

        try { execute(msg); }
        catch (TelegramApiException e) { log.error("Error showing main menu", e); }
    }

    public void sendTextMessage(Long chatId, String text) {
        SendMessage msg = new SendMessage();
        msg.setChatId(String.valueOf(chatId));
        msg.setText(text);
        try { execute(msg); }
        catch (TelegramApiException e) { log.error("Failed to send message: {}", e.getMessage(), e); }
    }
}