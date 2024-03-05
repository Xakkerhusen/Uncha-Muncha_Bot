package com.example.Uncha_Muncha_Bot.controller;

import com.example.Uncha_Muncha_Bot.constants.CommonConstants;
import com.example.Uncha_Muncha_Bot.constants.SuperAdminConstants;
import com.example.Uncha_Muncha_Bot.dto.ProfileDTO;
import com.example.Uncha_Muncha_Bot.enums.ActiveStatus;
import com.example.Uncha_Muncha_Bot.enums.Language;
import com.example.Uncha_Muncha_Bot.enums.ProfileRole;
import com.example.Uncha_Muncha_Bot.markUps.MarkUpsAdmin;
import com.example.Uncha_Muncha_Bot.markUps.MarkUps;
import com.example.Uncha_Muncha_Bot.markUps.MarkUpsSuperAdmin;
import com.example.Uncha_Muncha_Bot.markUps.MarkUpsUser;
import com.example.Uncha_Muncha_Bot.service.ProfileService;
import com.example.Uncha_Muncha_Bot.service.ResourceBundleService;
import io.github.nazarovctrl.telegrambotspring.bot.MessageSender;
import io.github.nazarovctrl.telegrambotspring.controller.AbstractUpdateController;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.FontFamily;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class UnchaMunchaBotController extends AbstractUpdateController {
    @Autowired
    private MessageSender messageSender;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private ResourceBundleService resourceBundleService;

    @Autowired
    private MarkUps markUps;

    @Autowired
    private MarkUpsSuperAdmin markUpsSuperAdmin;

    @Autowired
    private MarkUpsAdmin markUpsAdmin;

    @Autowired
    private MarkUpsUser markUpsUser;

    @Override
    public void handle(Update update) {
        if (update.hasMessage()) {
            ProfileDTO currentProfile = profileService.getByChatId(update.getMessage().getChatId().toString());
            /** Update Username*/
            if (currentProfile != null && update.getMessage().getFrom().getUserName() != null) {
                String userName = update.getMessage().getFrom().getUserName();
                if (!(currentProfile.getUsername() != null && currentProfile.getUsername().equals(userName))) {
                    updateUsername(update.getMessage().getChatId().toString(), userName);
                }
            }
            /**For checking (/start,/help, ...) commands*/
            if (currentProfile != null && currentProfile.getPhone() != null) {
                if (checkCommond(update, currentProfile)) {
                    return;
                }
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
                log.warn(e.getMessage());
                messageUser(update, currentProfile);
            }
        } else if (update.hasCallbackQuery()) {
            ProfileDTO currentProfile = profileService.getByChatId(update.getCallbackQuery().getMessage().getChatId().toString());
            /** Update Username*/
            if (currentProfile != null && update.getCallbackQuery().getFrom().getUserName() != null) {
                String userName = update.getCallbackQuery().getFrom().getUserName();
                if (!(currentProfile.getUsername() != null && currentProfile.getUsername().equals(userName))) {
                    updateUsername(update.getCallbackQuery().getMessage().getChatId().toString(), userName);
                }
            }
            assert currentProfile != null;
            if (currentProfile.getRole().equals(ProfileRole.SUPER_ADMIN)) {
                callBQSuperAdmin(update, currentProfile);
            } else if (currentProfile.getRole().equals(ProfileRole.ADMIN)) {
                callBQAdmin(update, currentProfile);
            } else {
                callBQUser(update, currentProfile);
            }
            return;
        }
    }

    // ===================================== USER ======================

    /**
     * For checking input message from User and return response
     */
    private void messageUser(Update update, ProfileDTO currentProfile) {
        Message message = update.getMessage();
        String chatId = message.getChatId().toString();
        if (currentProfile == null) {
            ProfileDTO profile = new ProfileDTO();
            profile.setChatId(chatId);
            profile.setUsername("@" + update.getMessage().getChat().getUserName());
            profile.setRole(ProfileRole.USER);
            profile.setActiveStatus(ActiveStatus.BLOCK);
            profile.setCurrentStep(CommonConstants.LANGUAGE);
            profile.setCreatedDateTime(LocalDateTime.now());
            if (profileService.save(profile) != null) {
                log.info("New profile created username :: " + profile.getUsername());

                SendMessage sendMessage1 = new SendMessage();
                String langUz = resourceBundleService.getMessage("choosing.language", Language.uz);
                String langTr = resourceBundleService.getMessage("choosing.language", Language.tr);
                String langRu = resourceBundleService.getMessage("choosing.language", Language.ru);
                String langEn = resourceBundleService.getMessage("choosing.language", Language.en);
                sendMessage1.setText(langUz + "\n" + langTr + "\n" + langRu + "\n" + langEn);
                sendMessage1.setChatId(chatId);
                sendMessage1.setReplyMarkup(markUps.language());
                executeMessage(sendMessage1);
                return;
            }
        } else if (currentProfile.getPhone() == null) {
            Language language = currentProfile.getLanguage();
            if (message.hasContact()) {
                Contact contact = message.getContact();
                ProfileDTO profile = new ProfileDTO();
                profile.setName(contact.getFirstName());
                profile.setSurname(contact.getLastName());
                profile.setPhone(contact.getPhoneNumber());
                profile.setActiveStatus(ActiveStatus.ACTIVE);
                if (profileService.saveContact(profile, chatId) != null) {
                    log.info("Profile`s contact saved: username :: " + profile.getUsername() + " name :: " + profile.getName());
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setText(resourceBundleService.getMessage("successful.registered", language));
                    sendMessage.setChatId(chatId);
                    ReplyKeyboardRemove keyboardRemove = new ReplyKeyboardRemove();
                    keyboardRemove.setRemoveKeyboard(true);
                    sendMessage.setReplyMarkup(keyboardRemove);
                    executeMessage(sendMessage);
                    SendMessage sendMessage1 = new SendMessage(chatId, resourceBundleService.getMessage("menu", language));
                    sendMessage1.setReplyMarkup(markUpsUser.menu(language));
                    executeMessage(sendMessage1);
                    profileService.changeStep(chatId, CommonConstants.MENU);
                    return;
                }
            } else if (language != null) {
                sendMessageAboutInvalidInput(language, chatId);
            }
        }
    }

    /**
     * For checking input callbackQuery from User and return response
     */
    private void callBQUser(Update update, ProfileDTO currentProfile) {
        CallbackQuery query = update.getCallbackQuery();
        String chatId = currentProfile.getChatId();
        if (currentProfile.getCurrentStep().equals(CommonConstants.LANGUAGE)) {
            if (currentProfile.getLanguage() == null) {
                if (query.getMessage().getText().contains(resourceBundleService.getMessage("choosing.language", Language.uz))
                        && query.getMessage().getText().contains(resourceBundleService.getMessage("choosing.language", Language.tr))) {
                    Language profileLanguage = Language.valueOf(query.getData());
                    profileService.changeLanguage(chatId, profileLanguage);
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(chatId);
                    sendMessage.setReplyMarkup(markUps.contactButton(profileLanguage));
                    sendMessage.setText(resourceBundleService.getMessage("please.click.the.send.phone.number.button", profileLanguage));
                    executeMessage(sendMessage);
                    profileService.changeStep(chatId, CommonConstants.SAND_CONTACT);
                    return;
                } else {
                    sendMessageAboutInvalidInput(currentProfile.getLanguage(), chatId);
                }

            } else {
                if (query.getMessage().getText().contains(resourceBundleService.getMessage("choosing.language", currentProfile.getLanguage()))) {
                    Language language = Language.valueOf(query.getData());
                    profileService.changeLanguage(chatId, Language.valueOf(query.getData()));
                    profileService.changeStep(chatId, CommonConstants.MENU);
                    SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("menu", language));
                    sendMessage.setReplyMarkup(markUpsUser.menu(language));
                    executeMessage(sendMessage);
                } else {
                    sendMessageAboutInvalidInput(currentProfile.getLanguage(), chatId);
                }
            }
        }
    }


    // ===================================== ADMIN ======================

    /**
     * For checking input message from Admin and return response
     */
    private void messageAdmin(Update update, ProfileDTO currentProfile) {

    }

    /**
     * For checking input callbackQuery from Admin and return response
     */
    private void callBQAdmin(Update update, ProfileDTO currentProfile) {
        CallbackQuery query = update.getCallbackQuery();
        String data = query.getData();
        String chatId = query.getMessage().getChatId().toString();

        List<String> languages = List.of("uz", "tr", "ru", "en");
        if (currentProfile.getCurrentStep().equals(CommonConstants.LANGUAGE)) {
            if (languages.contains(data)) {
                Language language = Language.valueOf(data);
                profileService.changeLanguage(chatId, Language.valueOf(data));
                profileService.changeStep(chatId, CommonConstants.MENU);
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("menu", language));
                sendMessage.setReplyMarkup(markUpsAdmin.menu(language));
                executeMessage(sendMessage);
            } else {
                sendMessageAboutInvalidInput(currentProfile.getLanguage(), chatId);

            }
        }
    }

    // ===================================== SUPER_ADMIN ================

    /**
     * For checking input message from SuperAdmin and return response
     */
    private void messageSuperAdmin(Update update, ProfileDTO currentProfile) {
        Message message = update.getMessage();
        String chatId = message.getChatId().toString();
        Language language = currentProfile.getLanguage();
        if (currentProfile.getCurrentStep().equals(SuperAdminConstants.GETTING_BY_CHAT_ID)) {
            try {
                Long profileChatId=Long.valueOf(message.getText());
                ProfileDTO profileDTO = profileService.getByChatId(profileChatId.toString());
                if (profileDTO != null) {
                    executeUserList(chatId, language, message, List.of(profileDTO), "profileByChatId");
                } else {
                    executeDeleteMessage(new DeleteMessage(chatId, message.getMessageId()));
                    executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("user.not.found", language)));
                    SendMessage sendMessage = new SendMessage(chatId, SuperAdminConstants.MENU);
                    sendMessage.setReplyMarkup(markUpsSuperAdmin.menu(language));
                    executeMessage(sendMessage);
                }
                profileService.changeStep(chatId,SuperAdminConstants.MENU);
            }catch (Exception e){
                log.warn(e.getMessage());
                sendMessageAboutInvalidInput(currentProfile.getLanguage(),chatId);
            }
        }
    }

    /**
     * For checking input callbackQuery from SuperAdmin and return response
     */
    private void callBQSuperAdmin(Update update, ProfileDTO currentProfile) {
        CallbackQuery query = update.getCallbackQuery();
        Message message = query.getMessage();
        String data = query.getData();
        String chatId = query.getMessage().getChatId().toString();
        String currentStep = currentProfile.getCurrentStep();
        Language profileLanguage = currentProfile.getLanguage();

        List<String> languages = List.of("uz", "tr", "ru", "en");
        if (currentStep.equals(CommonConstants.LANGUAGE)) {
            if (languages.contains(data)) {
                Language language = Language.valueOf(data);
                profileService.changeLanguage(chatId, Language.valueOf(data));
                profileService.changeStep(chatId, SuperAdminConstants.MENU);
                SendMessage sendMessage = new SendMessage(chatId, resourceBundleService.getMessage("super.admin.menu", language));
                sendMessage.setReplyMarkup(markUpsSuperAdmin.menu(language));
                executeMessage(sendMessage);
            } else {
                sendMessageAboutInvalidInput(profileLanguage, chatId);

            }
        } else if (currentStep.equals(SuperAdminConstants.MENU)) {
            if (data.equals(SuperAdminConstants.MAKE_ADMIN)) {
                profileService.changeStep(chatId, SuperAdminConstants.ENTERING_ID_FOR_MAKE_ADMIN);
                EditMessageText editMessageText = new EditMessageText(resourceBundleService.getMessage("enter.profile.id", profileLanguage));
                editMessageText.setMessageId(query.getMessage().getMessageId());
                editMessageText.setChatId(chatId);
                executeEditMessage(editMessageText);
            } else if (data.equals(SuperAdminConstants.MAKE_USER)) {
                profileService.changeStep(chatId, SuperAdminConstants.ENTERING_ID_FOR_MAKE_USER);
                EditMessageText editMessageText = new EditMessageText(resourceBundleService.getMessage("enter.profile.id", profileLanguage));
                editMessageText.setMessageId(query.getMessage().getMessageId());
                editMessageText.setChatId(chatId);
                executeEditMessage(editMessageText);
            } else if (data.equals(SuperAdminConstants.MAKE_ACTIVE)) {
                profileService.changeStep(chatId, SuperAdminConstants.ENTERING_ID_FOR_MAKE_ACTIVE);
                EditMessageText editMessageText = new EditMessageText(resourceBundleService.getMessage("enter.profile.id", profileLanguage));
                editMessageText.setMessageId(query.getMessage().getMessageId());
                editMessageText.setChatId(chatId);
                executeEditMessage(editMessageText);
            } else if (data.equals(SuperAdminConstants.MAKE_BLOCK)) {
                profileService.changeStep(chatId, SuperAdminConstants.ENTERING_ID_FOR_MAKE_BLOCK);
                EditMessageText editMessageText = new EditMessageText(resourceBundleService.getMessage("enter.profile.id", profileLanguage));
                editMessageText.setMessageId(query.getMessage().getMessageId());
                editMessageText.setChatId(chatId);
                executeEditMessage(editMessageText);
            } else if (data.equals(SuperAdminConstants.GET_ALL)) {
                sendAllProfileList(message, chatId, currentProfile.getLanguage());
            } else if (data.equals(SuperAdminConstants.GET_ALL_ADMIN)) {
                sendAllAdminList(message, chatId, currentProfile.getLanguage());
            } else if (data.equals(SuperAdminConstants.GET_ALL_USER)) {
                sendAllUserList(message, chatId, currentProfile.getLanguage());
            } else if (data.equals(SuperAdminConstants.GET_BY_CHAT_ID)) {
                sendUserByChatId(message, chatId, currentProfile.getLanguage());
            } else if (data.equals(SuperAdminConstants.CREATE_ADVERTISING)) {

            }
        }
    }


    private void sendAllProfileList(Message message, String chatId, Language language) {
        List<ProfileDTO> users = profileService.getAllByRole(List.of(ProfileRole.SUPER_ADMIN, ProfileRole.ADMIN, ProfileRole.USER));
        executeUserList(chatId, language, message, users, "all");
    }

    private void sendAllAdminList(Message message, String chatId, Language language) {
        List<ProfileDTO> users = profileService.getAllByRole(List.of(ProfileRole.ADMIN));
        executeUserList(chatId, language, message, users, "admins");
    }

    private void sendAllUserList(Message message, String chatId, Language language) {
        List<ProfileDTO> users = profileService.getAllByRole(List.of(ProfileRole.USER));
        executeUserList(chatId, language, message, users, "users");
    }

    private void sendUserByChatId(Message message, String chatId, Language language) {
        profileService.changeStep(chatId, SuperAdminConstants.GETTING_BY_CHAT_ID);
        executeDeleteMessage(new DeleteMessage(chatId, message.getMessageId()));
        executeMessage(new SendMessage(chatId, resourceBundleService.getMessage("send.profile.id", language)));
    }

    /**
     * For send given Profile List on Excel format
     */
    private void executeUserList(String chatId, Language language, Message message, List<ProfileDTO> users, String fileName) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet autoSheet = workbook.createSheet("user");

        XSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setFillPattern(FillPatternType.DIAMONDS);
        cellStyle.setFillForegroundColor(IndexedColors.AQUA.index);
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFamily(FontFamily.ROMAN);
        cellStyle.setFont(font);

        XSSFRow row1 = autoSheet.createRow(0);

        XSSFCell cellId = row1.createCell(0);
        cellId.setCellStyle(cellStyle);
        cellId.setCellValue("Id");

        XSSFCell cellChatId = row1.createCell(1);
        cellChatId.setCellStyle(cellStyle);
        cellChatId.setCellValue("ChatId");

        XSSFCell cellActiveStatus = row1.createCell(2);
        cellActiveStatus.setCellStyle(cellStyle);
        cellActiveStatus.setCellValue("activeStatus");

        XSSFCell cellPhone = row1.createCell(3);
        cellPhone.setCellStyle(cellStyle);
        cellPhone.setCellValue("phone");

        XSSFCell cellUsername = row1.createCell(4);
        cellUsername.setCellStyle(cellStyle);
        cellUsername.setCellValue("username");

        XSSFCell cellCreatedDateTime = row1.createCell(5);
        cellCreatedDateTime.setCellStyle(cellStyle);
        cellCreatedDateTime.setCellValue("createdDateTime");

        XSSFCell cellLatitude = row1.createCell(6);
        cellLatitude.setCellStyle(cellStyle);
        cellLatitude.setCellValue("latitude");

        XSSFCell cellLongitude = row1.createCell(7);
        cellLongitude.setCellStyle(cellStyle);
        cellLongitude.setCellValue("longitude");

        XSSFCell cellName = row1.createCell(8);
        cellName.setCellStyle(cellStyle);
        cellName.setCellValue("name");

        XSSFCell cellSurname = row1.createCell(9);
        cellSurname.setCellStyle(cellStyle);
        cellSurname.setCellValue("surname");

        XSSFCell cellRole = row1.createCell(10);
        cellRole.setCellStyle(cellStyle);
        cellRole.setCellValue("role");

        XSSFCell cellCurrentStep = row1.createCell(11);
        cellCurrentStep.setCellStyle(cellStyle);
        cellCurrentStep.setCellValue("currentStep");

        XSSFCell cellSelectedPurchaseType = row1.createCell(12);
        cellSelectedPurchaseType.setCellStyle(cellStyle);
        cellSelectedPurchaseType.setCellValue("selectedPurchaseType");

        XSSFCell cellChangingElementId = row1.createCell(12);
        cellChangingElementId.setCellStyle(cellStyle);
        cellChangingElementId.setCellValue("changingElementId");

        XSSFCell cellLanguage = row1.createCell(12);
        cellLanguage.setCellStyle(cellStyle);
        cellLanguage.setCellValue("language");

        int i = 0;
        for (
                ProfileDTO user : users) {
            XSSFRow row = autoSheet.createRow(++i);
            XSSFCell cell = row.createCell(0);
            cell.setCellValue(user.getId());
            XSSFCell cell1 = row.createCell(1);
            cell1.setCellValue(user.getChatId());
            XSSFCell cell2 = row.createCell(2);
            cell2.setCellValue(user.getActiveStatus().name());
            XSSFCell cell3 = row.createCell(3);
            cell3.setCellValue(user.getPhone());
            XSSFCell cell4 = row.createCell(4);
            cell4.setCellValue(user.getUsername());
            XSSFCell cell5 = row.createCell(5);
            cell5.setCellValue(user.getCreatedDateTime());
            XSSFCell cell6 = row.createCell(6);
            if (user.getLatitude() != null) {
                cell6.setCellValue(user.getLatitude());
            }
            XSSFCell cell7 = row.createCell(7);
            if (user.getLongitude() != null) {
                cell7.setCellValue(user.getLongitude());
            }
            XSSFCell cell8 = row.createCell(8);
            if (user.getName() != null) {
                cell8.setCellValue(user.getName());
            }
            XSSFCell cell9 = row.createCell(9);
            if (user.getSurname() != null) {
                cell9.setCellValue(user.getSurname());
            }
            XSSFCell cell10 = row.createCell(10);
            cell10.setCellValue(user.getRole().name());
            XSSFCell cell11 = row.createCell(11);
            cell11.setCellValue(user.getCurrentStep());
            XSSFCell cell12 = row.createCell(12);
            if (user.getSelectedPurchaseType() != null) {
                cell12.setCellValue(user.getSelectedPurchaseType().name());
            }
            XSSFCell cell13 = row.createCell(13);
            if (user.getChangingElementId() != null) {
                cell13.setCellValue(user.getChangingElementId());
            }
            XSSFCell cell14 = row.createCell(14);
            if (user.getLanguage() != null) {
                cell14.setCellValue(user.getLanguage().name());
            }

        }
        try {
            workbook.write(new FileOutputStream("C:\\Projects\\Uncha Muncha Bot\\Uncha-Muncha_Bot\\src\\main\\resources\\" + fileName + ".xlsx"));
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SendDocument sendDocument = new SendDocument(chatId, new InputFile(new File("C:\\Projects\\Uncha Muncha Bot\\Uncha-Muncha_Bot\\src\\main\\resources\\" + fileName + ".xlsx")));
        executeDocument(sendDocument);
        executeDeleteMessage(new DeleteMessage(chatId, message.getMessageId()));
        SendMessage sendMessage = new SendMessage(chatId, SuperAdminConstants.MENU);
        sendMessage.setReplyMarkup(markUpsSuperAdmin.menu(language));
        executeMessage(sendMessage);
    }

    // ===================================== EXECUTE ===================

    /**
     * For execute SendMessage
     */
    private boolean executeMessage(SendMessage sendMessage) {
        try {
            messageSender.execute(sendMessage);
            return true;
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * For update message
     * Last {Hellaa}
     * <p>
     * New {Hello}
     */
    private boolean executeEditMessage(EditMessageText editMessageText) {
        try {
            messageSender.execute(editMessageText);
            return true;
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * For execute Document
     */
    private void executeDocument(SendDocument sendDocument) {
        try {
            messageSender.execute(sendDocument);
        } catch (TelegramApiException e) {
            log.warn(e.getMessage());
        }
    }

    /**
     * For Delete Message
     */
    private void executeDeleteMessage(DeleteMessage deleteMessage) {
        try {
            messageSender.execute(deleteMessage);
        } catch (TelegramApiException e) {
            log.warn(e.getMessage());
        }
    }

    // ===================================== GENERAL ===================

    /**
     * For update profile username when username changed
     */
    private void updateUsername(String chatId, String userName) {
        profileService.updateUsername(chatId, "@" + userName);
    }

    /**
     * For checking (/start, /language, /help, /about_owners, /connection)
     */
    private boolean checkCommond(Update update, ProfileDTO currentProfile) {
        if (!(currentProfile != null&&currentProfile.getPhone()!=null)) {
            return false;
        }

        if (update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            String chatId = update.getMessage().getChatId().toString();
            if (text.equals("/start")) {
                switch (currentProfile.getRole()) {
                    case SUPER_ADMIN -> {
                        SendMessage message = new SendMessage();
                        message.setChatId(chatId);
                        message.setText(resourceBundleService.getMessage("super.admin.menu", currentProfile.getLanguage()));
                        message.setReplyMarkup(markUpsSuperAdmin.menu(currentProfile.getLanguage()));
                        executeMessage(message);
                        profileService.changeStep(chatId, SuperAdminConstants.MENU);
                        break;
                    }
                    case ADMIN -> {
                        SendMessage message = new SendMessage();
                        message.setChatId(chatId);
                        message.setText(resourceBundleService.getMessage("menu", currentProfile.getLanguage()));
                        message.setReplyMarkup(markUpsAdmin.menu(currentProfile.getLanguage()));
                        executeMessage(message);
                        profileService.changeStep(chatId, CommonConstants.MENU);
                        break;
                    }
                    case USER -> {
                        SendMessage message = new SendMessage();
                        message.setChatId(chatId);
                        message.setText(resourceBundleService.getMessage("menu", currentProfile.getLanguage()));
                        message.setReplyMarkup(markUpsUser.menu(currentProfile.getLanguage()));
                        executeMessage(message);
                        profileService.changeStep(chatId, CommonConstants.MENU);
                        break;
                    }
                }
                return true;
            } else if (text.equals("/language")) {
                SendMessage message = new SendMessage();
                message.setChatId(chatId);
                message.setText(resourceBundleService.getMessage("choosing.language", currentProfile.getLanguage()));
                message.setReplyMarkup(markUps.language());
                executeMessage(message);
                profileService.changeStep(chatId, CommonConstants.LANGUAGE);
                return true;
            } else if (text.equals("/help")) {
                ProfileDTO superAdmin = profileService.getSuperAdmin();
                if (superAdmin == null) {
                    superAdmin = profileService.getByChatId("2035107903");
                }
                String helpText1 = resourceBundleService.getMessage("help.1", currentProfile.getLanguage());
                String helpText2 = resourceBundleService.getMessage("help.2", currentProfile.getLanguage());
                SendMessage message = new SendMessage();
                message.setChatId(chatId);
                message.setText(helpText1 + superAdmin.getUsername() + " " + helpText2);
                executeMessage(message);
                return true;
            } else if (text.equals("/about_owners")) {
                Language language = currentProfile.getLanguage();
                StringBuilder info = new StringBuilder(resourceBundleService.getMessage("bot.info.1", language));

                /**The usernames of the owners are added here*/
                for (ProfileDTO dto : profileService.getOwnersList()) {
                    info.append("\n").append(dto.getUsername()).append(",");
                }

                /**Here the (,) after the last username is removed*/
                info = new StringBuilder(info.substring(0, info.length() - 1));
                info.append(resourceBundleService.getMessage("bot.info.2", language));

                /** The total number of users is added here */
                info.append(profileService.getCount());
                info.append(resourceBundleService.getMessage("bot.info.3", language));

                executeMessage(new SendMessage(chatId, info.toString()));
                return true;
            } else if (text.equals("/connection")) {
                StringBuilder builder = new StringBuilder(" ");
                List<ProfileDTO> adminList = profileService.getAdminList();
                for (ProfileDTO dto : adminList) {
                    builder.append("\n").append(dto.getUsername()).append(", ");
                }

                if (adminList.size() != 0) {
                    builder = new StringBuilder(builder.substring(0, builder.length() - 2));
                } else {
                    try {
                        builder.append(profileService.getByChatId("994001445").getUsername());
                    } catch (Exception e) {
                        log.warn(e.getMessage());
                    }
                }
                executeMessage(new SendMessage(chatId, builder.toString()));
                return true;
            }
        }
        return false;
    }

    /**
     * For sand message (Entered invalid message)
     */
    private void sendMessageAboutInvalidInput(Language language, String chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(resourceBundleService.getMessage("invalid.query.entered", language));
        sendMessage.setChatId(chatId);
        executeMessage(sendMessage);
    }

}
