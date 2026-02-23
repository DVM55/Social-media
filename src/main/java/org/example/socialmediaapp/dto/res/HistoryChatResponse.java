package org.example.socialmediaapp.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.socialmediaapp.dto.ChatDto;

import java.util.List;

@Data
@AllArgsConstructor
public class HistoryChatResponse {
    private final Long conversationId;
    private final boolean hasMore;
    private final List<ChatDto> data;
}

