package com.example.Uncha_Muncha_Bot.markUps;

import com.example.Uncha_Muncha_Bot.enums.Language;
import com.example.Uncha_Muncha_Bot.service.ResourceBundleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.LinkedList;
import java.util.List;

@Component
public class MarkUps {
    @Autowired
    private ResourceBundleService resourceBundleService;

    public ReplyKeyboard adminButton(Language language) {
        return null;
    }

    public ReplyKeyboard language() {
        List<InlineKeyboardButton> buttonsRow = new LinkedList<>();
        List<List<InlineKeyboardButton>> rowList = new LinkedList<>();

        InlineKeyboardButton button = new InlineKeyboardButton();

        button.setText(Language.uz.name()+" \uD83C\uDDFA\uD83C\uDDFF");
        button.setCallbackData(Language.uz.name());

        buttonsRow.add(button);
        button = new InlineKeyboardButton();

        button.setText(Language.tr.name()+" \uD83C\uDDF9\uD83C\uDDF7");
        button.setCallbackData(Language.tr.name());

        buttonsRow.add(button);
        button = new InlineKeyboardButton();

        button.setText(Language.ru.name()+" \uD83C\uDDF7\uD83C\uDDFA");
        button.setCallbackData(Language.ru.name());

        buttonsRow.add(button);
        button = new InlineKeyboardButton();

        button.setText(Language.en.name()+" \uD83C\uDDEC\uD83C\uDDE7");
        button.setCallbackData(Language.en.name());

        buttonsRow.add(button);
        rowList.add(buttonsRow);

        return new InlineKeyboardMarkup(rowList);
    }

    public ReplyKeyboard contactButton(Language profileLanguage) {
        KeyboardRow row = new KeyboardRow();
        KeyboardButton button = new KeyboardButton(resourceBundleService.getMessage("sharing.phone.number",profileLanguage));
        button.setRequestContact(true);
        row.add(button);
        return ReplyKeyboardMarkup.builder()
                .keyboard(List.of(row))
                .resizeKeyboard(true)
                .build();
    }
}
