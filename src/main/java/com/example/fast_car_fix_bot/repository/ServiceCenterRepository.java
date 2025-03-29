package com.example.fast_car_fix_bot.repository;

import com.example.fast_car_fix_bot.service.ServiceCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface ServiceCenterRepository extends JpaRepository<ServiceCenter, Long> {



    @Query(value = "SELECT * FROM service_center s WHERE ST_Distance(s.location, ST_SetSRID(ST_MakePoint(?2, ?1), 4326)) < 10000", nativeQuery = true)
    List<ServiceCenter> findNearby(@Param("latitude") double latitude, @Param("longitude") double longitude);

    @Query("SELECT s FROM ServiceCenter s WHERE LOWER(s.city) = LOWER(:city)")
    List<ServiceCenter> findByCity(@Param("city") String city);

}




