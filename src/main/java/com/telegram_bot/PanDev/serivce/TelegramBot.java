package com.telegram_bot.PanDev.serivce;

import com.telegram_bot.PanDev.config.TelegramBotConfig;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;


import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;
import java.io.IOException;;
import java.util.HashMap;
import java.util.Map;

/**
 * Основной класс Telegram бота, обрабатывающий команды пользователя
 * Включает команды для добавления и удаления категорий, просмотра дерева и выгрузки дерева категорий в Excel
 */
@Slf4j
@Service
public class TelegramBot extends TelegramLongPollingBot {

    private final TelegramBotConfig botConfig;

    private final CategoryService categoryService;

    private final Map<Long, String> lastCommands = new HashMap<>();

    public TelegramBot(TelegramBotConfig botConfig, CategoryService categoryService) {
        this.botConfig = botConfig;
        this.categoryService = categoryService;
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken(){
        return botConfig.getBotToken();
    }

    /**
     * Обрабатывает сообщения от пользователей, распознавая команды
     * Вызывет соответствующие методы для каждой команды
     */
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();

            Long chatId = update.getMessage().getChatId();
            if (chatId == null) {
                log.error("Chat ID is null. Cannot proceed with the command.");
                return;
            }

            // Разбиваем текст сообщения на команду и аргументы
            String[] parts = messageText.split(" ");
            String command = parts[0];

            switch (command) {
                case "/start":
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;

                case "/viewTree":
                    sendMessage(chatId, categoryService.formatTree());
                    break;

                case "/addElement":
                    if (parts.length == 3) {  // Проверяем, что команда содержит два аргумента
                        String parentName = parts[1];
                        String elementName = parts[2];
                        sendMessage(chatId, categoryService.addElement(parentName, elementName));
                    } else {
                        sendMessage(chatId, "Неверный формат. Используйте /addElement <родитель> <название>");
                    }
                    break;

                case "/removeElement":
                    if (parts.length == 2) {  // Проверяем, что команда содержит один аргумент
                        String elementName = parts[1];
                        sendMessage(chatId, categoryService.removeElement(elementName));
                    } else {
                        sendMessage(chatId, "Неверный формат. Используйте /removeElement <название>");
                    }
                    break;

                case "/download":
                    try {
                        byte[] excelData = categoryService.exportCategoriesToExcel();
                        SendDocument document = new SendDocument();
                        document.setChatId(String.valueOf(chatId));
                        document.setDocument(new InputFile(new ByteArrayInputStream(excelData), "categories.xlsx"));
                        execute(document);
                    } catch (IOException | TelegramApiException e) {
                        log.error("Ошибка при отправке документа: " + e.getMessage());
                        sendMessage(chatId, "Произошла ошибка при выгрузке файла.");
                    }
                    break;

                case "/upload":
                    sendMessage(chatId, "Отправьте файл Excel для импорта.");
                    lastCommands.put(chatId, "/upload");
                    break;

                case "/help":
                    sendMessage(chatId, help());
                    break;

                default:
                    sendMessage(chatId, "Команда не распознана. Используйте /help для справки.");
            }
        }

    }
    private String help() {
        return """
                Доступные команды:
                /viewTree - Просмотр дерева категорий
                /addElement <родитель> <название> - Добавление категории
                /removeElement <название> - Удаление категории
                /download - Скачивать Excel документ с деревом категорий.
                /upload - Отправить Excel документ с деревом категорий и сохранить все элементы в базе данных.
                /help - Вывод справки
                """;
    }
    /**
     * Метод, который выводит приветственное сообщение
     */
    private void startCommandReceived(long chatId, String name) {
        String answer = "Привет! " + name + " используй следующие команды, чтобы построить дерево!" + "\n" + help();
        log.info("replied to user " + answer);
        sendMessage(chatId, answer);
    }
    /**
     * Метод, который принимает в качестве параметра id пользователя и необходимый текст для отправкм пользователю
     */
    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        log.info("replied to user " + message);
        try {
            execute(message);
        } catch (TelegramApiException e){
            log.error("Error occurred" + e.getMessage());
        }

    }

}
