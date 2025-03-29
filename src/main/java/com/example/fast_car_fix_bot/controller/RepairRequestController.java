package com.example.fast_car_fix_bot.controller;

import com.example.fast_car_fix_bot.entity.RepairRequest;
import com.example.fast_car_fix_bot.repository.RepairRequestRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Slf4j
@RestController
@RequestMapping("/api/requests")
public class RepairRequestController {

    private final RepairRequestRepository repository;

    public RepairRequestController(RepairRequestRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<RepairRequest> getAllRequests() {
        return repository.findAll();
    }

    @PostMapping
    public RepairRequest createRequest(@RequestBody RepairRequest request) {
        log.info("New repair request created: {}", request);
        return repository.save(request);
    }
}