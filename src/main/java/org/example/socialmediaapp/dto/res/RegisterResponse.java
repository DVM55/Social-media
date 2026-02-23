package org.example.socialmediaapp.dto.res;


import lombok.Data;
import org.example.socialmediaapp.enums.Role;

import java.time.LocalDateTime;

@Data
public class RegisterResponse {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private LocalDateTime createdAt;
}

