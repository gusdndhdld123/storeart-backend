package com.example.storeartbackend.DTO;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class CallcenterDTO {

    private Long callcenterIdx;              // 기본 PK
    private int userIdx;          // 작성자
    private String title;            // 제목
    private String content;         //내용
    private String category;      // 카테고리
    private String complete;          // 답변완료 Y/N
    private LocalDateTime regdate;
    private LocalDateTime moddate;
    private String writer;
}
