package com.example.fast_car_fix_bot.entity;

import com.example.fast_car_fix_bot.service.RepairRequestStatus;
import com.example.fast_car_fix_bot.service.Step;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

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
    private ZonedDateTime repairDate;
    private String price;

    @Enumerated(EnumType.STRING)
    private RepairRequestStatus status = RepairRequestStatus.NEW;
    private double latitude;
    private double longitude;

    @Enumerated(EnumType.STRING)
    private Step currentStep;

    public RepairRequest(Long userId, String description) {
        this.userId = userId;
        this.description = description;
        this.status = RepairRequestStatus.NEW;
    }

    @Override
    public String toString() {
        return "RepairRequest{" +
                "id=" + id +
                ", userId=" + userId +
                ", serviceCenterId=" + serviceCenterId +
                ", description='" + description + '\'' +
                ", repairDate=" + repairDate +
                ", price='" + price + '\'' +
                ", status=" + status +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", currentStep=" + currentStep +
                '}';
    }
}