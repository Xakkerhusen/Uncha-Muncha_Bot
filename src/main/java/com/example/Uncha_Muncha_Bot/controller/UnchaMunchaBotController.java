package com.example.Uncha_Muncha_Bot.controller;

import com.example.Uncha_Muncha_Bot.dto.ProfileDTO;
import com.example.Uncha_Muncha_Bot.enums.ProfileRole;
import com.example.Uncha_Muncha_Bot.service.ProfileService;
import io.github.nazarovctrl.telegrambotspring.bot.MessageSender;
import io.github.nazarovctrl.telegrambotspring.controller.AbstractUpdateController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class UnchaMunchaBotController extends AbstractUpdateController {
    @Autowired
    private MessageSender messageSender;

    @Autowired
    private ProfileService profileService;

    @Override
    public void handle(Update update) {
        if (update.hasCallbackQuery()) {
            ProfileDTO currentProfile = profileService.getByChatId(update.getCallbackQuery().getMessage().getChatId().toString());
            if (currentProfile.getRole().equals(ProfileRole.SUPER_ADMIN)) {
                callBQSuperAdmin(update, currentProfile);
            } else if (currentProfile.getRole().equals(ProfileRole.ADMIN)) {
                callBQAdmin(update, currentProfile);
            } else {
                callBQUser(update, currentProfile);
            }
            return;
        } else if (update.hasMessage()) {
            ProfileDTO currentProfile = profileService.getByChatId(update.getMessage().getChatId().toString());
            if (checkCommon(update, currentProfile)){
                return;
            }
            try {
                if (currentProfile != null && !currentProfile.getRole().equals(ProfileRole.USER)) {
                    if (currentProfile.getRole().equals(ProfileRole.SUPER_ADMIN)) {
                        messageSuperAdmin(update, currentProfile);
                    } else if (currentProfile.getRole().equals(ProfileRole.ADMIN)) {
                        messageAdmin(update, currentProfile);
                    }
                } else {
                    messageUser(update, currentProfile);
                }
            } catch (Exception e) {
                e.printStackTrace();
                messageUser(update, currentProfile);
            }
        }
        try {
            messageSender.execute(new SendMessage(update.getMessage().getChatId().toString(),"Nma Gap Mazgi"));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    // For checking (/start, /language, /help, /about_owners, /connection)
    private boolean checkCommon(Update update, ProfileDTO currentProfile) {
        if (update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            if (text.equals("/start")) {
                // logic
                return true;
            }else if (text.equals("/language")) {
                // logic
                return true;
            } else if (text.equals("/help")) {
                // logic
                return true;
            } else if (text.equals("/about_owners")) {
                // logic
                return true;
            } else if (text.equals("/connection")) {
                // logic
                return true;
            }
        }
        return false;
    }

    // ===================================== USER ======================
    private void messageUser(Update update, ProfileDTO currentProfile) {

    }
    private void callBQUser(Update update, ProfileDTO currentProfile) {

    }

    // ===================================== ADMIN ======================
    private void messageAdmin(Update update, ProfileDTO currentProfile) {

    }
    private void callBQAdmin(Update update, ProfileDTO currentProfile) {

    }

    // ===================================== SUPER_ADMIN =================
    private void messageSuperAdmin(Update update, ProfileDTO currentProfile) {

    }
    private void callBQSuperAdmin(Update update, ProfileDTO currentProfile) {

    }
}
