package com.example.storeartbackend.DTO;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DateLimitDTO {
    private Long dateLimitIdx;
    private int userIdx;
    private String startDate;
    private String endDate;
    private LocalDateTime regdate;
    private LocalDateTime moddate;
}
