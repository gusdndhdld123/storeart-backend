package com.example.storeartbackend.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "RankTracking")
public class RankTrackingEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long RankTrackingIdx;
    private int userIdx;
    private String keyword;
    private String productName;
    private String storeName;
    private String nvmid;
}
