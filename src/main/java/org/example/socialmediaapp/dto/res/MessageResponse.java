package org.example.socialmediaapp.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.socialmediaapp.dto.MessageDto;
import org.example.socialmediaapp.dto.UserDto;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageResponse {
    private Long conversationId;
    private UserDto sender;
    private MessageDto message;
    private Integer unRead;
}
