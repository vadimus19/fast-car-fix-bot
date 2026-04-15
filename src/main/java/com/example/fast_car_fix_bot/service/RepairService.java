package com.example.fast_car_fix_bot.service;

import com.example.fast_car_fix_bot.dto.RepairRequestCreateDto;
import com.example.fast_car_fix_bot.dto.RepairRequestResponseDto;
import com.example.fast_car_fix_bot.entity.RepairRequest;
import com.example.fast_car_fix_bot.entity.ServiceCenter;
import com.example.fast_car_fix_bot.enums.RepairRequestStatus;
import com.example.fast_car_fix_bot.exception.RequestProcessingException;
import com.example.fast_car_fix_bot.exception.ResourceNotFoundException;
import com.example.fast_car_fix_bot.mapper.RepairRequestMapper;
import com.example.fast_car_fix_bot.repository.RepairRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class RepairService {

    private final RepairRequestRepository repairRequestRepository;
    private final RepairRequestMapper mapper;
    private final NotificationService notificationService;

    public List<RepairRequestResponseDto> getAllRequests() {
        return repairRequestRepository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public RepairRequestResponseDto createRequest(RepairRequestCreateDto dto) {
        try {
            RepairRequest entity = mapper.toEntity(dto);
            entity.setStatus(RepairRequestStatus.NEW);

            RepairRequest saved = repairRequestRepository.save(entity);
            return mapper.toDto(saved);

        } catch (Exception e) {
            log.error("Error creating repair request for user {}", dto.getUserId(), e);
            throw new RequestProcessingException("Failed to create repair request", e);
        }
    }

    @Async
    @Transactional
    public void processRepairRequestAsync(RepairRequest repairRequest) {
        try {
            RepairRequest saved = repairRequestRepository.save(repairRequest);

            log.info("Repair request saved asynchronously: {}", saved);

            notificationService.notifyUser(
                    saved.getUserId(),
                    "Your repair request has been received! We will process it shortly."
            );

        } catch (Exception e) {
            log.error("Failed to process repair request asynchronously", e);
            throw new RequestProcessingException(
                    "Failed to process repair request asynchronously", e
            );
        }
    }

    @Transactional
    public void updateRepairRequestStatus(Long repairRequestId, RepairRequestStatus status) {
        RepairRequest repairRequest = repairRequestRepository.findById(repairRequestId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Repair request with id " + repairRequestId + " not found"
                ));

        repairRequest.setStatus(status);
        repairRequestRepository.save(repairRequest);
    }

    public Optional<RepairRequest> findActiveRequestByUser(Long chatId) {
        return repairRequestRepository.findFirstByUserIdAndStatusInOrderByIdDesc(
                chatId,
                Arrays.asList(RepairRequestStatus.NEW, RepairRequestStatus.IN_PROGRESS)
        );
    }

    public RepairRequest saveRepairRequest(RepairRequest request) {
        return repairRequestRepository.save(request);
    }

    public List<ServiceCenter> findNearbyServiceCenters(double latitude, double longitude) {
        return List.of();
    }
}