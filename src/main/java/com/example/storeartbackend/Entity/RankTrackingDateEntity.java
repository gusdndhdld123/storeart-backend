package com.example.storeartbackend.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "RankTrackingDate")
public class RankTrackingDateEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long RankTrackingDateIdx;
    @Column(name = "rank_tracking_idx")
    private Long rankTrackingIdx; // RankTracking의 ID 참조
    private String nvmid;
    private String keyword;
    private String rank;
    private LocalDate date;
}
