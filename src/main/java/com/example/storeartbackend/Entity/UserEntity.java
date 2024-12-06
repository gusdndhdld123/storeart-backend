package com.example.storeartbackend.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "User")
@SequenceGenerator(
        name = "User_seq",
        sequenceName = "User_seq",
        initialValue = 1,
        allocationSize = 1
)
public class UserEntity extends BaseEntity{

    @Id
    @Column(name = "UserIdx")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int userIdx;
    // 이메일
    @Column(unique = true)
    private String userEmail;
    //Id
    @Column(nullable = true)
    private String userId;
    //비밀번호
    private String userPassword;
    //이름
    private String userName;
    //소속회사
    private String organization;
    //폰번호
    @Column(nullable = true)
    private String userPhone;;
    //허가받았는가? 기본 N 관리자 허가 시 Y
    private String useYn;
    //주소
    private String userAddress;
    //상세주소
    private String userDetailaddress;

    //마케팅 수신동의
    private String mktEmail;
    private String mktSms;
    private String mktAdr;


    private String grade;

}