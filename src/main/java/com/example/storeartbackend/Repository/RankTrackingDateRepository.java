package com.example.storeartbackend.Repository;

import com.example.storeartbackend.Entity.RankTrackingDateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
@Repository
public interface RankTrackingDateRepository extends JpaRepository<RankTrackingDateEntity, Long> {
    List<RankTrackingDateEntity> findByDate(LocalDate date);
    List<RankTrackingDateEntity> findByRankTrackingIdx(Long rankTrackingIdx);
    void deleteByrankTrackingIdx(Long rankTrackingIdx);
}

