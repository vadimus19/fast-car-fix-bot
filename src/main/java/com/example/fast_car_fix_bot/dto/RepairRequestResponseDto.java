package com.example.fast_car_fix_bot.dto;

import com.example.fast_car_fix_bot.enums.RepairRequestStatus;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class RepairRequestResponseDto {
    private Long id;
    private Long userId;
    private String description;
    private ZonedDateTime repairDate;
    private RepairRequestStatus status;
}
