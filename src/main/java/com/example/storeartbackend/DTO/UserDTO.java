package com.example.storeartbackend.DTO;

import jakarta.persistence.Column;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {
    private int userIdx;
    //이메일
    @Column(unique = true)
    private String userEmail;
    //id
    private String userId;
    //비밀번호
    private String userPassword;
    //이름
    private String userName;
    //소속회사
    private String organization;
    //폰번호
    private String userPhone;
    //주소
    private String userAddress;
    //상세주소
    private String userDetailaddress;
    //허가받았는가? 기본 N 관리자 허가 시 Y
    private String useYn;
    //마케팅 수신동의
    private String mktEmail;
    private String mktSms;
    private String mktAdr;
    private LocalDateTime regdate;
    private LocalDateTime moddate;
    private String grade;
}
