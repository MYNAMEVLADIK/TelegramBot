package com.example.telegrembot.listener;

import com.example.telegrembot.service.NotificationTaskService;
import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TelegramBotUpdatesListenerTest {

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private NotificationTaskService notificationTaskService;

    @InjectMocks
    private TelegramBotUpdateListener telegramBotUpdateListener;

    @Test
    public void initTest() {
        telegramBotUpdateListener.init();
    }

    @Test
    public void handleStartTest() throws URISyntaxException, IOException {
        checkResponse("/start", """
                🔥🔥🔥Привет👋, меня зовут Планировщик🕒, я помогу тебе с твоими делами😁, а именно с тем чтобы ты их не забыл!
                Для этого тебе нужно записать для меня точную дату, время и событие в формате {12.12.2222 День рождение Друга!}
                А я не дам тебе про него забыть😉!🔥🔥🔥
                """);
    }

    @Test
    public void handleIncorrectInputDateTimeFormatTest() throws URISyntaxException, IOException {
        checkResponse("35.50.2023 30:30 Принести конфет", "⚠️Неккоректный формат даты и/или времени!⚠️");
    }

    @Test
    public void handleIncorrectInputFormatTest() throws URISyntaxException, IOException {
        checkResponse("20.12.2023 20:20 Vlad", "⚠️Неккоректный формат сообщения!⚠️");
    }

    @Test
    public void handleCorrectInputFormatTest() throws URISyntaxException, IOException {
        checkResponse("20.12.2023 20:40 Принести конфет", "Задача успешно запланирована!" +
                "Я напомню тебе, когда нужно будет ее выполнить!");
    }

    private void checkResponse(String input, String expectedOutput) throws URISyntaxException, IOException {
        String json = Files.readString(
                Path.of(Objects.requireNonNull(TelegramBotUpdatesListenerTest.class.getResource("danger.json")).toURI()));
        Update updateWithIncorrectDateTime = BotUtils.fromJson(
                json.replace("%text%", input), Update.class);
        SendResponse sendResponse = BotUtils.fromJson(
                "{'ok': true}", SendResponse.class);

        when(telegramBot.execute(any())).thenReturn(sendResponse);

        telegramBotUpdateListener.process(Collections.singletonList(updateWithIncorrectDateTime));

        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
        Mockito.verify(telegramBot).execute(argumentCaptor.capture());
        SendMessage actual = argumentCaptor.getValue();

        Assertions.assertEquals(actual.getParameters().get("chat_id"),
                updateWithIncorrectDateTime.message().chat().id());
        Assertions.assertEquals(actual.getParameters().get("text"),
                expectedOutput);
    }
}
