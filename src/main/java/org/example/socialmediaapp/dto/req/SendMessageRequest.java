package org.example.socialmediaapp.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendMessageRequest {
    @NotBlank(message = "Nội dung gửi không được để trống")
    private String content;
}
