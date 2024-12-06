package com.example.storeartbackend.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RankTrackingDateEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long RankTrackingDateIdx;
    private int RankTrackingIdx; // RankTracking의 ID 참조
    private String nvmid;
    private String keyword;
    private String rank;
    private LocalDate date;
}
