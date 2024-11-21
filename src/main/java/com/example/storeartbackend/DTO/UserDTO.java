package com.example.storeartbackend.DTO;

import jakarta.persistence.Column;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {
    private int userIdx;
    //가입 시 id를 이메일로 받음
    @Column(unique = true)
    private String userEmail;
    //비밀번호
    private String userPassword;
    //이름
    private String userName;
    //소속회사
    private String organization;
    //폰번호
    private int phoneNumber;
    //허가받았는가? 기본 N 관리자 허가 시 Y
    private String useYn;
}
