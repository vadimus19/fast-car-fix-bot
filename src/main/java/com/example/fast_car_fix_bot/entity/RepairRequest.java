package com.example.fast_car_fix_bot.entity;

import com.example.fast_car_fix_bot.service.RepairRequestStatus;
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

    private double latitude;   // Добавляем поле для широты
    private double longitude;  // Добавляем поле для долготы

    public RepairRequest(Long userId, String description) {
        this.userId = userId;
        this.description = description;
        this.status = RepairRequestStatus.NEW;
    }

    // Переопределение метода toString()
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
                ", latitude=" + latitude +   // Добавляем вывод широты
                ", longitude=" + longitude + // Добавляем вывод долготы
                '}';
    }

    // Геттеры и сеттеры для latitude и longitude
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}