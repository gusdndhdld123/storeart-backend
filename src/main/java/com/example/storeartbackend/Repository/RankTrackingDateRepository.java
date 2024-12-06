package com.example.storeartbackend.Repository;

import com.example.storeartbackend.Entity.RankTrackingDateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RankTrackingDateRepository extends JpaRepository<RankTrackingDateEntity, Long> {
    List<RankTrackingDateEntity> findByDate(LocalDate date);
}
