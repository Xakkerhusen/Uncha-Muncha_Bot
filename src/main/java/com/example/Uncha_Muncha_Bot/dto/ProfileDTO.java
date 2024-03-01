package com.example.Uncha_Muncha_Bot.dto;


import com.example.Uncha_Muncha_Bot.enums.ActiveStatus;
import com.example.Uncha_Muncha_Bot.enums.Language;
import com.example.Uncha_Muncha_Bot.enums.ProfileRole;
import com.example.Uncha_Muncha_Bot.enums.SelectedPurchaseType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ProfileDTO {
    private Integer id;
    private ActiveStatus acctiveStatus;
    private String phone;
    private String username;
    private LocalDateTime createdDateTime;
    private Double latitude;
    private Double longitude;
    private String name;
    private String surname;
    private String chatId;
    private ProfileRole role;
    private String currentStep;
    private SelectedPurchaseType selectedPurchaseType;
    private Integer changingElementId;
    private Language language=Language.uz;

    public ProfileDTO(Integer id, String username, String chatId) {
        this.id = id;
        this.username = username;
        this.chatId = chatId;
    }
}
