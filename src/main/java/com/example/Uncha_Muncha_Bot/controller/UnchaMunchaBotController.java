package com.example.Uncha_Muncha_Bot.controller;

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

    @Override
    public void handle(Update update) {
        try {
            messageSender.execute(new SendMessage(update.getMessage().getChatId().toString(),"Nma Gap Mazgi"));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
