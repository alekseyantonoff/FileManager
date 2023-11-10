package com.manager.service.impl;

import com.manager.dao.AppUserDao;
import com.manager.dao.RawDataDao;
import com.manager.entity.AppUser;
import com.manager.entity.RawData;
import com.manager.service.MainService;
import com.manager.service.ProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import static com.manager.entity.enums.UserState.BASIC_STATE;
import static com.manager.entity.enums.UserState.WAIT_FOR_EMAIL_STATE;
import static com.manager.service.enums.ServiceCommands.CANCEL;
import static com.manager.service.enums.ServiceCommands.HELP;
import static com.manager.service.enums.ServiceCommands.REGISTRATION;
import static com.manager.service.enums.ServiceCommands.START;

@Service
@RequiredArgsConstructor
@Slf4j
public class MainServiceImpl implements MainService {
    private final RawDataDao rawDataDao;
    private final ProducerService producerService;
    private final AppUserDao appUserDao;

//    public MainServiceImpl(RawDataDao rawDataDao, ProducerService producerService) {
//        this.rawDataDao = rawDataDao;
//        this.producerService = producerService;
//    }

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var userState = appUser.getState();
        var text = update.getMessage().getText();
        var output = "";

        if (CANCEL.equals(text)) {
            output = cancelProcess(appUser);
        } else if (BASIC_STATE.equals(userState)) {
            output = processeServiceCommand(appUser, text);
        } else if (WAIT_FOR_EMAIL_STATE.equals(userState)) {
            //TODO: Добавить обработку email
        } else {
            log.info("Unknown user state: " + userState);
            output = "Неизвестная ошибка! Введите /cancel и попробуйте снова!";
        }

        var chatId = update.getMessage().getChatId();
        sendAnswer(output, chatId);


    }

    @Override
    public void processDocMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var chatId = update.getMessage().getChatId();
        if (isNotAllowToSendContent(chatId, appUser)) {
            return;
        }
        //TODO: Добавить метод сохранения документа
        var answer = "Документ успешно загружен! Ссылка для скачивания: http://test.ru/get-doc/000";
        sendAnswer(answer, chatId);
    }

    @Override
    public void processPhotoMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var chatId = update.getMessage().getChatId();
        if (isNotAllowToSendContent(chatId, appUser)) {
            return;
        }
        //TODO: Добавить метод сохранения фото
        var answer = "Фотография успешно загружена! Ссылка для скачивания: http://test.ru/get-photo/000";
        sendAnswer(answer, chatId);
    }

    private boolean isNotAllowToSendContent(Long chatId, AppUser appUser) {
        var userState = appUser.getState();
        if (!appUser.getIsActive()) {
            var error = "Зарегистрируйтесь или активируйте свою учетную запись для загрузки контента.";
            sendAnswer(error, chatId);
            return true;
        } else if (!BASIC_STATE.equals(userState)) {
            var error = "Отменить текущую команда с помощью /cancel для отправки файлов.";
            sendAnswer(error, chatId);
            return true;
        }
        return false;
    }

    private void sendAnswer(String output, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        producerService.producerAnswer(sendMessage);
    }

    private String processeServiceCommand(AppUser appUser, String cmd) {
        if (REGISTRATION.equals(cmd)) {
            //TODO: Добавить регистрацию
            return "Временно недоступно.";
        } else if (HELP.equals(cmd)) {
            return help();
        } else if (START.equals(cmd)) {
            return "Привет! Для просмотра списка доступных команд введи /help";
        } else {
            return "Неизвестная команда! Для просмотра списка доступных команд введи /help";
        }
    }

    private String help() {
        return "список доступных команд:\n"
                + "/cancel - отмена выполнения текущей команды;\n"
                + "/registration - регистрация пользователя;\n";
    }

    private String cancelProcess(AppUser appUser) {
        appUser.setState(BASIC_STATE);
        appUserDao.save(appUser);
        return "команда отменена!";
    }

    private AppUser findOrSaveAppUser(Update update) {
        User telegramUser = update.getMessage().getFrom();
        AppUser persistantAppUser = appUserDao.findAppUserByTelegramUserId(telegramUser.getId());
        if (persistantAppUser == null) {
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .userName(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    //TODO: Изменить значение по умолчанию после добавление регистрации
                    .isActive(true)
                    .state(BASIC_STATE)
                    .build();
            return appUserDao.save(transientAppUser);
        }
        return persistantAppUser;
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
                .event(update)
                .build();
        rawDataDao.save(rawData);
    }
}
