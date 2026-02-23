package org.example.socialmediaapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatDto {
    private Long senderId;
    private MessageDto message;
}
