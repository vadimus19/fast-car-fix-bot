package com.example.fast_car_fix_bot.repository;

import com.example.fast_car_fix_bot.controller.CarRepairBot;
import com.example.fast_car_fix_bot.entity.RepairRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional // технически ок, но все методы в классе делать транзакционными не стоит. Ставь только где необходимо
public class RepairService {

    private final RepairRequestRepository repairRequestRepository;

    @Autowired
    private final CarRepairBot bot;

    public RepairService(RepairRequestRepository repairRequestRepository, @Lazy CarRepairBot bot) {
        this.repairRequestRepository = repairRequestRepository;
        this.bot = bot;
    }

    @Async
    @Transactional
    public void processMessage(Long chatId, String message, double latitude, double longitude) {
        try {
            // Save the repair request in the database
            RepairRequest repairRequest = new RepairRequest(chatId, message);
            repairRequest.setLatitude(latitude);
            repairRequest.setLongitude(longitude);
            repairRequestRepository.save(repairRequest);

            log.info("Repair request saved for user {}: {}", chatId, message);

            // Send confirmation message to the user
            bot.sendTextMessage(chatId, "Your request has been received! Please wait for further information.");
        } catch (Exception e) {
            log.error("Error processing repair request for user {}: ", chatId, e);
            bot.sendTextMessage(chatId, "An error occurred while processing your request. Please try again later.");
        }
    }
}