package com.example.fast_car_fix_bot.service;

import com.example.fast_car_fix_bot.dto.RepairRequestCreateDto;
import com.example.fast_car_fix_bot.dto.RepairRequestResponseDto;
import com.example.fast_car_fix_bot.entity.RepairRequest;
import com.example.fast_car_fix_bot.enums.RepairRequestStatus;
import com.example.fast_car_fix_bot.enums.Step;
import com.example.fast_car_fix_bot.mapper.RepairRequestMapper;
import com.example.fast_car_fix_bot.repository.RepairRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RepairService {

    private final RepairRequestRepository repairRequestRepository;
    private final RepairRequestMapper mapper;

    // ================= DTO =================

    public List<RepairRequestResponseDto> getAllRequests() {
        return repairRequestRepository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public RepairRequestResponseDto createRequest(RepairRequestCreateDto dto) {

        RepairRequest entity = mapper.toEntity(dto);

        entity.setStatus(RepairRequestStatus.IN_PROGRESS);
        entity.setCurrentStep(Step.TYPING_DESCRIPTION);

        return mapper.toDto(repairRequestRepository.save(entity));
    }

    // ================= BOT FLOW =================

    public RepairRequest createNewRequest(Long userId) {

        RepairRequest request = new RepairRequest();
        request.setUserId(userId);

        // ❗ ВАЖНО: иначе findActiveRequestByUser не найдёт заявку
        request.setStatus(RepairRequestStatus.IN_PROGRESS);

        // стартовый шаг FSM
        request.setCurrentStep(Step.SELECTING_PROBLEM);

        return repairRequestRepository.save(request);
    }

    public Optional<RepairRequest> findActiveRequestByUser(Long userId) {

        return repairRequestRepository
                .findFirstByUserIdAndStatusInOrderByIdDesc(
                        userId,
                        List.of(
                                RepairRequestStatus.NEW,
                                RepairRequestStatus.IN_PROGRESS
                        )
                );
    }

    public void save(RepairRequest request) {
        repairRequestRepository.save(request);
    }
}