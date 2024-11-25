package com.telegram_bot.PanDev;

import com.telegram_bot.PanDev.config.TelegramBotConfig;
import com.telegram_bot.PanDev.serivce.CategoryService;
import com.telegram_bot.PanDev.serivce.TelegramBot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import static org.mockito.Mockito.*;

public class TelegramBotTest {

    @Mock
    private TelegramBotConfig botConfig;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private TelegramBot telegramBot;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testOnUpdateReceivedAddElement() {
        Update update = mockUpdate("/addElement Parent Child");

        when(categoryService.addElement("Parent", "Child")).thenReturn("Категория добавлена.");
        telegramBot.onUpdateReceived(update);

        verify(categoryService).addElement("Parent", "Child");
    }

    @Test
    void testOnUpdateReceivedRemoveElement() {
        Update update = mockUpdate("/removeElement Child");

        when(categoryService.removeElement("Child")).thenReturn("Категория удалена.");
        telegramBot.onUpdateReceived(update);

        verify(categoryService).removeElement("Child");
    }

    @Test
    void testOnUpdateReceivedDownload() throws Exception {
        Update update = mockUpdate("/download");

        byte[] excelData = new byte[]{1, 2, 3};
        when(categoryService.exportCategoriesToExcel()).thenReturn(excelData);

        telegramBot.onUpdateReceived(update);

        verify(categoryService).exportCategoriesToExcel();
    }

    private Update mockUpdate(String text) {
        Update update = new Update();
        Message message = mock(Message.class);
        when(message.getText()).thenReturn(text);
        when(message.getChatId()).thenReturn(123456L);
        when(update.getMessage()).thenReturn(message);
        return update;
    }
}
