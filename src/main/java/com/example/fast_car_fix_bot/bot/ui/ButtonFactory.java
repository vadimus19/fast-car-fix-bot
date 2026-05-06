package com.example.fast_car_fix_bot.bot.ui;

import com.example.fast_car_fix_bot.enums.RepairServiceType;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Arrays;
import java.util.List;

public class ButtonFactory {

    public static InlineKeyboardButton button(String text, String data) {
        InlineKeyboardButton b = new InlineKeyboardButton();
        b.setText(text);
        b.setCallbackData(data);
        return b;
    }

    public static List<List<InlineKeyboardButton>> serviceMenu() {
        return Arrays.stream(RepairServiceType.values())
                .map(t -> List.of(button(t.getDescription(), t.name())))
                .toList();
    }

    public static List<List<InlineKeyboardButton>> actionMenu() {
        return List.of(
                List.of(button("Nearby", "NEARBY")),
                List.of(button("New request", "NEW_REQUEST")),
                List.of(button("Main menu", "MAIN_MENU")),
                List.of(button("Back", "BACK"))
        );
    }

    public static List<List<InlineKeyboardButton>> backMenu() {
        return List.of(
                List.of(button("Back", "BACK")),
                List.of(button("Main menu", "MAIN_MENU"))
        );
    }
}
