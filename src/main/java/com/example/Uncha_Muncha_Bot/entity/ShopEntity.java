package com.example.Uncha_Muncha_Bot.entity;

import com.example.Uncha_Muncha_Bot.dto.ShopTypeDTO;
import com.example.Uncha_Muncha_Bot.enums.ActiveStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Setter
@Getter
public class ShopEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ElementCollection
    @CollectionTable(name = "shop_type", joinColumns = @JoinColumn(name = "shop_id"))
    @Column(name = "shop_types")
    private List<ShopTypeDTO> shopTypes;

    @Column(name = "city")
    private String city;

    @Column(name = "district")
    private String district;

    @Column(name = "info")
    private String info;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "phone")
    private String phone;

    @Column(name = "username")
    private String username;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "created_date_time")
    private LocalDateTime createdDateTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "active_status")
    private ActiveStatus activeStatus;

    @Column(name = "owner_chat_id")
    private String ownerChatId;
}
