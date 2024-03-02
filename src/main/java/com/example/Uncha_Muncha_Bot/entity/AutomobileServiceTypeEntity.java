package com.example.Uncha_Muncha_Bot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AutomobileServiceTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @Column(name = "auto_service_id")
    private Integer autoServiceId;
    @Column(name = "service_name")
    private String serviceName;
}
