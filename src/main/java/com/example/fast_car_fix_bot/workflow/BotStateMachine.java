package com.example.fast_car_fix_bot.workflow;

import com.example.fast_car_fix_bot.bot.ui.ButtonFactory;
import com.example.fast_car_fix_bot.entity.RepairRequest;
import com.example.fast_car_fix_bot.enums.BotAction;
import com.example.fast_car_fix_bot.enums.RepairServiceType;
import com.example.fast_car_fix_bot.enums.Step;
import com.example.fast_car_fix_bot.service.BotResponseService;
import com.example.fast_car_fix_bot.service.NearbyService;
import com.example.fast_car_fix_bot.service.RepairService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class BotStateMachine {

    private final RepairService repairService;
    private final BotResponseService response;
    private final NearbyService nearby;

    public BotStateMachine(RepairService repairService,
                           BotResponseService response,
                           NearbyService nearby) {
        this.repairService = repairService;
        this.response = response;
        this.nearby = nearby;
    }

    public void showServiceMenu(RepairRequest req) {
        response.sendWithButtons(
                req.getUserId(),
                "Choose problem:",
                ButtonFactory.serviceMenu()
        );
    }

    public void showActionMenu(RepairRequest req) {
        response.sendWithButtons(
                req.getUserId(),
                "What next?",
                ButtonFactory.actionMenu()
        );
    }

    public void handle(RepairRequest req, Message msg) {

        switch (req.getCurrentStep()) {

            case TYPING_DESCRIPTION -> handleDescription(req, msg);

            default -> response.send(req.getUserId(), "Unsupported step");
        }
    }

    public void handleCallback(RepairRequest req, String data) {

        try {
            BotAction action = BotAction.valueOf(data);

            switch (action) {
                case BACK -> goBack(req);

                case MAIN_MENU -> {
                    goMain(req);
                    showServiceMenu(req);
                    return;
                }

                case NEW_REQUEST -> newRequest(req);

                case NEARBY -> showNearby(req);
            }

            return;

        } catch (IllegalArgumentException ignored) {

        }

        try {
            RepairServiceType type = RepairServiceType.valueOf(data);
            selectProblem(req, type);
        } catch (Exception e) {
            response.send(req.getUserId(), "Unknown action");
        }
    }

    private void selectProblem(RepairRequest req, RepairServiceType type) {

        req.setServiceType(type);
        req.setCurrentStep(Step.TYPING_DESCRIPTION);

        repairService.save(req);

        response.sendWithButtons(
                req.getUserId(),
                "Describe problem:",
                ButtonFactory.backMenu()
        );
    }

    private void handleDescription(RepairRequest req, Message msg) {

        req.setDescription(msg.getText());
        req.setCurrentStep(Step.SHARING_LOCATION);

        repairService.save(req);

        response.sendLocationRequest(req.getUserId(), "📍 Send your location");
    }

    private void showNearby(RepairRequest req) {

        if (req.getLatitude() == null) {
            response.send(req.getUserId(), "Send location first");
            return;
        }

        var list = nearby.findNearest(req.getLatitude(), req.getLongitude());

        StringBuilder sb = new StringBuilder("Nearby:\n\n");

        for (var c : list) {
            sb.append("• ").append(c.getName()).append("\n");
        }

        response.send(req.getUserId(), sb.toString());
    }

    private void goBack(RepairRequest req) {
        if (req.getCurrentStep() == Step.TYPING_DESCRIPTION) {
            req.setCurrentStep(Step.SELECTING_PROBLEM);
            repairService.save(req);
            showServiceMenu(req);
        }
    }

    private void goMain(RepairRequest req) {

        req.setCurrentStep(Step.SELECTING_PROBLEM);
        req.setServiceType(null);
        req.setDescription(null);
        req.setLatitude(null);
        req.setLongitude(null);

        repairService.save(req);
    }

    private void newRequest(RepairRequest req) {
        goMain(req);
        showServiceMenu(req);
    }
}