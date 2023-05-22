package com.example.telegrembot.listener;

import com.example.telegrembot.entity.NotificationTask;
import com.example.telegrembot.service.NotificationTaskService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TelegramBotUpdateListener implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdateListener.class);

    private final Pattern pattern = Pattern.compile("(\\d{1,2}\\.\\d{1,2}\\.\\d{4} \\d{1,2}:\\d{2})\\s+([А-я\\d\\s.,!?:]+)");

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final TelegramBot telegramBot;

    private final NotificationTaskService notificationTaskService;

    public TelegramBotUpdateListener(TelegramBot telegramBot, NotificationTaskService notificationTaskService) {
        this.telegramBot = telegramBot;
        this.notificationTaskService = notificationTaskService;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> list) {
        try {
            list.stream()
                    .filter(update -> update.message() != null)
                    .forEach(update -> {
                        logger.info("Handles update: {}", update);
                        Message message = update.message();
                        Long chatId = message.chat().id();
                        String text = message.text();

                        if ("/start".equals(text)) {
                            sendMessage(chatId, """
                                    🔥🔥🔥Привет👋, меня зовут Планировщик🕒, я помогу тебе с твоими делами😁, а именно с тем чтобы ты их не забыл!
                                    Для этого тебе нужно записать для меня точную дату, время и событие в формате {12.12.2222 День рождение Друга!}
                                    А я не дам тебе про него забыть😉!🔥🔥🔥
                                    """);
                        } else if (text != null) {
                            Matcher matcher = pattern.matcher(text);
                            if (matcher.find()) {
                                LocalDateTime dateTime = parse(matcher.group(1));
                                if (Objects.isNull(dateTime)) {
                                    sendMessage(chatId, "⚠️Неккоректный формат даты и/или времени!⚠️");
                                } else {
                                    String text1 = matcher.group(2);
                                    NotificationTask notificationTask = new NotificationTask();
                                    notificationTask.setChatId(chatId);
                                    notificationTask.setMessage(text1);
                                    notificationTask.setNotificationDateTime(dateTime);
                                    notificationTaskService.save(notificationTask);
                                    sendMessage(chatId, "Задача успешно запланирована!" +
                                            "Я напомню тебе, когда нужно будет ее выполнить!");
                                }
                            } else {
                                sendMessage(chatId, "⚠️Неккоректный формат сообщения!⚠️");
                            }
                        }
                    });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    @Nullable
    private LocalDateTime parse(String dateTime) {
        try {
            return LocalDateTime.parse(dateTime, dateTimeFormatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private void sendMessage(Long chatId, String message) {
        SendMessage sendMessage = new SendMessage(chatId, message);
        SendResponse sendResponse = telegramBot.execute(sendMessage);
        if (!sendResponse.isOk()) {
            logger.error("Error during sending message: {}", sendResponse.description());
        }
    }
}
