package com.example.fast_car_fix_bot.mapper;

import com.example.fast_car_fix_bot.dto.RepairRequestCreateDto;
import com.example.fast_car_fix_bot.dto.RepairRequestResponseDto;
import com.example.fast_car_fix_bot.entity.RepairRequest;
import com.example.fast_car_fix_bot.enums.RepairRequestStatus;
import org.springframework.stereotype.Component;

@Component
public class RepairRequestMapper {

    public RepairRequestResponseDto toDto(RepairRequest entity) {
        RepairRequestResponseDto dto = new RepairRequestResponseDto();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setDescription(entity.getDescription());
        dto.setRepairDate(entity.getRepairDate());
        dto.setStatus(entity.getStatus());
        return dto;
    }

    public RepairRequest toEntity(RepairRequestCreateDto dto) {
        RepairRequest entity = new RepairRequest();
        entity.setUserId(dto.getUserId());
        entity.setDescription(dto.getDescription());
        entity.setLatitude(dto.getLatitude());
        entity.setLongitude(dto.getLongitude());
        entity.setStatus(RepairRequestStatus.NEW);
        return entity;
    }
}