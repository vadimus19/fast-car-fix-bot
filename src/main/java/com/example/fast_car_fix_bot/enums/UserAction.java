package com.example.fast_car_fix_bot.enums;

public enum UserAction {
    CALL_CENTER("Call Center"),
    GET_DIRECTIONS("Get Directions");

    private final String label;

    UserAction(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
