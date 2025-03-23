package com.example.fast_car_fix_bot.repository;

import com.example.fast_car_fix_bot.controller.CarRepairBot;
import com.example.fast_car_fix_bot.entity.RepairRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional // технически ок, но все методы в классе делать транзакционными не стоит. Ставь только где необходимо
public class RepairService {

    private final RepairRequestRepository repairRequestRepository;
    private final CarRepairBot bot;

    public RepairService(RepairRequestRepository repairRequestRepository, CarRepairBot bot) {
        this.repairRequestRepository = repairRequestRepository;
        this.bot = bot;
    }

    @Async
    public void processMessage(Long chatId, String message) {
        try {
            RepairRequest request = new RepairRequest();
            request.setUserId(chatId);
            request.setDescription(message);
            request.setStatus("New"); // хороший кандидаьт для enum
            repairRequestRepository.save(request);

            log.info("Request received for user {}: {}", chatId, message);

            bot.sendTextMessage(chatId, "✅ Your request has been accepted! Please wait for a response from the auto repair shop.");

        } catch (Exception e) {
            log.error("Error processing request: ", e);
            bot.sendTextMessage(chatId, "❌ An error occurred while processing your request. Please try again later.");
        }
    }
}