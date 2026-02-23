package org.example.socialmediaapp.dto.req;

import lombok.Data;
import org.example.socialmediaapp.enums.MessageType;

@Data
public class SendFileRequest {
    private String objectKey;
    private String fileName;
    private MessageType messageType;
}
