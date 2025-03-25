package com.example.fast_car_fix_bot.repository;

import com.example.fast_car_fix_bot.entity.RepairRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository // без аннотации тоже будет работать
public interface RepairRequestRepository extends JpaRepository<RepairRequest, Long> {
    List<RepairRequest> findByRepairDate(String repairDate); // метод не используется
}
