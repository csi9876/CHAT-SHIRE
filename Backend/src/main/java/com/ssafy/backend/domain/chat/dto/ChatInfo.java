package com.ssafy.backend.domain.chat.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.ssafy.backend.domain.chat.entity.Chat;
import com.ssafy.backend.domain.chat.entity.ChatRoom;
import com.ssafy.backend.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
public class ChatInfo {
    private Long userId;
    private String content;
    private Long chatNumber;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime chatTime;
    private Boolean isAttached;

    public Chat toEntity(User user, ChatRoom chatRoom) {
        return Chat.builder()
                .user(user)
                .chatRoom(chatRoom)
                .content(this.content)
                .chatTime(this.chatTime)
                .chatNumber(this.chatNumber)
                .isAttached(this.isAttached).build();
    }

    public ChatInfo(Long userId, String content, Long chatNumber, LocalDateTime chatTime, Boolean isAttached) {
        this.userId = userId;
        this.content = content;
        this.chatNumber = chatNumber;
        this.chatTime = chatTime;
        this.isAttached = isAttached;
    }
}
