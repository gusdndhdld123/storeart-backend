package com.example.storeartbackend.DTO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class UserLoginDTO {
    private String userId;
    private String userPw;
}
