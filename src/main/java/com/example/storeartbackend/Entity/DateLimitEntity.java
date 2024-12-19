package com.example.storeartbackend.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "datelimit")
@Builder
public class DateLimitEntity extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dateLimitIdx;
    private int userIdx;
    private String startDate;
    private String endDate;

}
