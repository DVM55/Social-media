package org.example.socialmediaapp.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.socialmediaapp.dto.MessageDto;
import org.example.socialmediaapp.dto.UserDto;

@Data
@AllArgsConstructor
public class ConversationResponse {
    private Long conversationId;
    private UserDto partner;
    private MessageDto message;
    private Integer unread;
}
