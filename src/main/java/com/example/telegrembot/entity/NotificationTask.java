package com.example.telegrembot.entity;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table (name = "notification_task")
@Getter
@Setter
public class NotificationTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column (name = "message")
    private String message;
    @Column (name = "notification_date_time", nullable = false)
    private LocalDateTime notificationDateTime;
    @Column (name = "chat_id", nullable = false)
    private Long chatId;
}
