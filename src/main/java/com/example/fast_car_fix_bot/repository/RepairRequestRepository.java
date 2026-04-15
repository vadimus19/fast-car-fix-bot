package com.example.fast_car_fix_bot.repository;

import com.example.fast_car_fix_bot.entity.RepairRequest;
import com.example.fast_car_fix_bot.enums.RepairRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface RepairRequestRepository extends JpaRepository<RepairRequest, Long> {

    List<RepairRequest> findByStatus(RepairRequestStatus status);

    Optional<RepairRequest> findFirstByUserIdAndStatusInOrderByIdDesc(Long userId, List<RepairRequestStatus> statuses);
}
