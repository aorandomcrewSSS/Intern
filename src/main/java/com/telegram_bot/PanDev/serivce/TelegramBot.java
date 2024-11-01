package com.telegram_bot.PanDev.serivce;

import com.telegram_bot.PanDev.config.TelegramBotConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Service
public class TelegramBot extends TelegramLongPollingBot {

    private final TelegramBotConfig botConfig;

    private final CategoryService categoryService;

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

    @Override
    public void onUpdateReceived(Update update) {
        String response;
        if(update.hasMessage() && update.getMessage().hasText()){
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            String[] parts = messageText.split(" ");

            switch (parts[0]) {
                case "/start":
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/viewTree":
                    sendMessage(chatId, categoryService.formatTree());
                    break;
                case "/addElement":
                    if (parts.length == 3) {
                        sendMessage(chatId, categoryService.addElement(parts[1], parts[2]));
                    } else {
                        sendMessage(chatId, "Неверный формат. Используйте /addElement <родитель> <название>");
                    }
                    break;
                case "/removeElement":
                    if (parts.length == 2) {
                        sendMessage(chatId, categoryService.removeElement(parts[1]));
                    } else {
                        sendMessage(chatId, "Неверный формат. Используйте /removeElement <название>");
                    }
                    break;
                case "/help":
                    sendMessage(chatId, help());
                    break;
                default:
                    sendMessage(chatId, "Sorry, unknown command");
            }
        }

    }
    private String help() {
        return """
                Доступные команды:
                /viewTree - Просмотр дерева категорий
                /addElement <родитель> <название> - Добавление категории
                /removeElement <название> - Удаление категории
                /help - Вывод справки
                """;
    }
    private void startCommandReceived(long chatId, String name) {
        String answer = "Привет! " + name + " используй следующие команды, чтобы построить дерево!" + "\n" + help();
        log.info("replied to user " + answer);
        sendMessage(chatId, answer);
    }

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
