package com.example.fast_car_fix_bot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class RepairRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long serviceCenterId;
    private String description;
    private String repairDate;
    private String price;
    private String status;

    public RepairRequest(Long userId, String description) {
        this.userId = userId;
        this.description = description;
        this.status = "New";
    }
}
