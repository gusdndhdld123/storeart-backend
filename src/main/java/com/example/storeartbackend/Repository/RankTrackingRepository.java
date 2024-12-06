package com.example.storeartbackend.Repository;

import com.example.storeartbackend.Entity.RankTrackingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RankTrackingRepository extends JpaRepository<RankTrackingEntity, Long> {
}
