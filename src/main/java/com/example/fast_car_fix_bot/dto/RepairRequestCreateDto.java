package com.example.fast_car_fix_bot.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RepairRequestCreateDto {

    @NotNull
    private Long userId;

    @NotBlank
    @Size(min = 5, max = 500)
    private String description;

    private Double latitude;   // ✅ FIX
    private Double longitude;  // ✅ FIX
}