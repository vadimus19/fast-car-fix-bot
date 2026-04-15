package com.example.fast_car_fix_bot.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RepairRequestCreateDto {

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotBlank(message = "Description cannot be empty")
    @Size(min = 5, max = 500, message = "Description must be between 5 and 500 characters")
    private String description;

    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
    private double latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
    private double longitude;
}