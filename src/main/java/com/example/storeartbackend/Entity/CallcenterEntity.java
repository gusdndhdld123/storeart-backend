package com.example.storeartbackend.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Callcenter")
@Builder
public class CallcenterEntity extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long callcenterIdx;              // 기본 PK
    private int userIdx;          // 작성자
    private String title;            // 제목
    private String content;         //내용
    private String category;      // 해당 날짜의 검색 횟수
    private String complete;          // 답변완료 Y/N
}
