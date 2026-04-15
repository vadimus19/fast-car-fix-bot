package com.example.fast_car_fix_bot.service;

import com.example.fast_car_fix_bot.TelegramBot.CarRepairBot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TelegramNotificationService implements NotificationService {
    private final CarRepairBot bot;

    @Override
    public void notifyUser(Long userId, String message) {
        bot.sendTextMessage(userId, message);
    }

}
