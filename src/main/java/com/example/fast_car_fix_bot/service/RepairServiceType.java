package com.example.fast_car_fix_bot.service;

public enum RepairServiceType {
    OIL_CHANGE("Oil Change"),
    TIRE_CHANGE("Tire Change"),
    ELECTRICIAN("Electrician"),
    GLASS_REPLACEMENT("Glass Replacement"),
    CHASSIS_DIAGNOSTICS("Chassis Diagnostics"),
    ENGINE_DIAGNOSTICS("Engine Diagnostics"),
    TOWING("Towing");

    private final String description;

    RepairServiceType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
