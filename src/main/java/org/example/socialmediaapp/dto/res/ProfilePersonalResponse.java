package org.example.socialmediaapp.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.socialmediaapp.enums.Gender;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfilePersonalResponse {
    private Long id;
    private String username;
    private String email;
    private String avatarUrl;
    private String phone;
    private LocalDate date_of_birth;
    private String address;
    private String gender;
    private Long count_follower;
    private Long count_folowing;
    private Long count_post;
}
