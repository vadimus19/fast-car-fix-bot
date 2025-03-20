package com.example.fast_car_fix_bot.controller;

import com.example.fast_car_fix_bot.entity.RepairRequest;
import com.example.fast_car_fix_bot.repository.RepairRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class RepairRequestController {

    private final RepairRequestRepository repository;

    @Autowired
    public RepairRequestController(RepairRequestRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<RepairRequest> getAllRequests() {
        return repository.findAll();
    }

    @PostMapping
    public RepairRequest createRequest(@RequestBody RepairRequest request) {
        return repository.save(request);
    }
}
