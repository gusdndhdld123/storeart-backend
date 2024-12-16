package com.example.storeartbackend.DTO;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RankTrackingDateDTO {
    private Long RankTrackingDateIdx;
    private int RankTrackingIdx; // RankTracking의 ID 참조
    private String nvmid;
    private String keyword;
    private String rank;
    private LocalDate date;

    private LocalDateTime regdate;
    private LocalDateTime moddate;
}
