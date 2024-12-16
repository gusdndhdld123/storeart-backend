package com.example.storeartbackend.DTO;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RankTrackingDTO {
    private Long RankTrackingIdx;
    private int userIdx;
    private String keyword;
    private String nvmid;
    private String productName;
    private String storeName;
    private LocalDateTime regdate;
    private LocalDateTime moddate;
}
