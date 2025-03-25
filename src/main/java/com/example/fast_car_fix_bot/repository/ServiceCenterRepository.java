package com.example.fast_car_fix_bot.repository;

import com.example.fast_car_fix_bot.service.ServiceCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ServiceCenterRepository extends JpaRepository<ServiceCenter, Long> {
    @Query("SELECT s FROM ServiceCenter s WHERE FUNCTION('ST_Distance_Sphere', POINT(s.longitude, s.latitude), POINT(:longitude, :latitude)) < 10000")
    List<ServiceCenter> findNearby(@Param("latitude") double latitude, @Param("longitude") double longitude);

    @Query("SELECT s FROM ServiceCenter s WHERE LOWER(s.city) = LOWER(:city)")
    List<ServiceCenter> findByCity(@Param("city") String city);
}

