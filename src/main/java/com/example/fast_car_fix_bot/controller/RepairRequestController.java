package com.example.fast_car_fix_bot.controller;

import com.example.fast_car_fix_bot.dto.RepairRequestCreateDto;
import com.example.fast_car_fix_bot.dto.RepairRequestResponseDto;
import com.example.fast_car_fix_bot.service.RepairService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/requests")
public class RepairRequestController {

    private final RepairService repairService;

    public RepairRequestController(RepairService repairService) {
        this.repairService = repairService;
    }

    @GetMapping
    public List<RepairRequestResponseDto> getAllRequests() {
        return repairService.getAllRequests();
    }

    @PostMapping
    public RepairRequestResponseDto createRequest(@Valid @RequestBody RepairRequestCreateDto dto) {
        log.info("Creating repair request for userId={}", dto.getUserId());
        return repairService.createRequest(dto);
    }
}