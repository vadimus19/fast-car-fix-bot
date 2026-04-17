package com.example.fast_car_fix_bot.bot;

import com.example.fast_car_fix_bot.entity.RepairRequest;
import com.example.fast_car_fix_bot.enums.*;
import com.example.fast_car_fix_bot.service.BotResponseService;
import com.example.fast_car_fix_bot.service.RepairService;
import com.example.fast_car_fix_bot.workflow.BotStateMachine;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service
public class BotFlowService {

    private final RepairService repairService;
    private final BotStateMachine stateMachine;
    private final BotResponseService response;

    public BotFlowService(RepairService repairService,
                          BotStateMachine stateMachine,
                          BotResponseService response) {
        this.repairService = repairService;
        this.stateMachine = stateMachine;
        this.response = response;
    }

    public void handleMessage(Message message) {

        Long userId = message.getChatId();

        // LOCATION
        if (message.hasLocation()) {

            RepairRequest req = repairService.findActiveRequestByUser(userId).orElse(null);

            if (req == null) {
                response.send(userId, "Press /start");
                return;
            }

            req.setLatitude(message.getLocation().getLatitude());
            req.setLongitude(message.getLocation().getLongitude());
            req.setCurrentStep(Step.SHOWING_NEARBY);

            repairService.save(req);

            stateMachine.showActionMenu(req);
            return;
        }

        String text = message.getText();

        // START
        if ("/start".equalsIgnoreCase(text)) {

            RepairRequest req = repairService.createNewRequest(userId);

            req.setStatus(RepairRequestStatus.IN_PROGRESS);
            req.setCurrentStep(Step.SELECTING_PROBLEM);

            repairService.save(req);

            stateMachine.showServiceMenu(req);
            return;
        }

        RepairRequest req = repairService.findActiveRequestByUser(userId).orElse(null);

        if (req == null) {
            response.send(userId, "Press /start");
            return;
        }

        stateMachine.handle(req, message);
    }

    public void handleCallback(CallbackQuery callback) {

        Long userId = callback.getMessage().getChatId();

        RepairRequest req = repairService.findActiveRequestByUser(userId).orElse(null);

        if (req == null) {
            response.send(userId, "Press /start");
            return;
        }

        stateMachine.handleCallback(req, callback.getData());
        response.answerCallback(callback.getId());
    }
}