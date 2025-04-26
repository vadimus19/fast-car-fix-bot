package com.example.fast_car_fix_bot.repository;

import com.example.fast_car_fix_bot.TelegramBot.CarRepairBot;
import com.example.fast_car_fix_bot.entity.RepairRequest;
import com.example.fast_car_fix_bot.service.RepairRequestStatus;
import com.example.fast_car_fix_bot.service.Step;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class RepairService {
    private final RepairRequestRepository repairRequestRepository;
    private final CarRepairBot bot;

    @Autowired
    public RepairService(RepairRequestRepository repairRequestRepository, @Lazy CarRepairBot bot) {
        this.repairRequestRepository = repairRequestRepository;
        this.bot = bot;
    }

    @Async
    @Transactional
    public void processMessage(Long chatId, String message, double latitude, double longitude) {
        try {
            RepairRequest repairRequest = new RepairRequest();
            repairRequest.setUserId(chatId);
            repairRequest.setDescription(message);
            repairRequest.setLatitude(latitude);
            repairRequest.setLongitude(longitude);
            repairRequest.setCurrentStep(Step.TYPING_DESCRIPTION);
            repairRequest.setStatus(RepairRequestStatus.NEW);

            repairRequestRepository.save(repairRequest);
            log.info("Repair request saved for user {}: {}", chatId, message);
            bot.sendTextMessage(chatId, "Your request has been received! Please wait for further information.");

        } catch (Exception e) {
            log.error("Error processing repair request for user {}: ", chatId, e);
            bot.sendTextMessage(chatId, "An error occurred while processing your request. Please try again later.");
        }
    }

    @Transactional
    public void updateRepairRequestStatus(Long repairRequestId, RepairRequestStatus status) {
        RepairRequest repairRequest = repairRequestRepository.findById(repairRequestId).orElseThrow(() -> new RuntimeException("Repair request not found"));
        repairRequest.setStatus(status);
        repairRequestRepository.save(repairRequest);
    }
}