package com.example.storeartbackend.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "naversearchcount")
public class NaverSearchCountEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;              // 기본 PK
    private int userIdx;          // 사용자 고유 ID
    private int grade;            // 사용자 등급
    private int searchCount;      // 해당 날짜의 검색 횟수
    private Integer maxSearch;    // 최대 검색 횟수 (무료 유저는 30, 무제한은 없음)
    private String date;          // 날짜 (하루 단위로 초기화)
}
