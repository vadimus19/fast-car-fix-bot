package com.example.fast_car_fix_bot.repository;

import com.example.fast_car_fix_bot.entity.RepairRequest;
import com.example.fast_car_fix_bot.service.RepairRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface RepairRequestRepository extends JpaRepository<RepairRequest, Long> {
    List<RepairRequest> findByStatus(RepairRequestStatus status);
}
