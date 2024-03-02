package com.example.Uncha_Muncha_Bot.dto;

import com.example.Uncha_Muncha_Bot.enums.ActiveStatus;
import com.example.Uncha_Muncha_Bot.enums.PharmacyType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Setter
@Getter
public class PharmacyDTO {
    private Long id;
    private PharmacyType pharmacyType;
    private LocalTime startTime;
    private LocalTime endTime;
    private String username;
    private String phone;
    private String pharmacyName;
    private String info;
    private Double latitude;
    private Double longitude;
    private ActiveStatus activeStatus;
    private LocalDateTime createdDateTime;
    private String ownerChatId;
}
