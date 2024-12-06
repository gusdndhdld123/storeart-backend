package com.example.storeartbackend.DTO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.bind.annotation.GetMapping;

@Getter
@Setter
@ToString
public class UserInfo {
    private String profileImageUrl;
    private String nickname;
    private String email;
}