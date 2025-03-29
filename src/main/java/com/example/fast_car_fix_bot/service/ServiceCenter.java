package com.example.fast_car_fix_bot.service;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Point;


@Entity
@Getter
@Setter
public class ServiceCenter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address;
    private String phone;
    private double latitude;
    private double longitude;
    private String city;

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point location;

}
