package com.example.storeartbackend.Repository;

import com.example.storeartbackend.Entity.RankTrackingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface RankTrackingRepository extends JpaRepository<RankTrackingEntity, Long> {
    Optional<RankTrackingEntity> findByUserIdxAndKeywordAndNvmid(int userIdx, String keyword, String nvmid);
    List<RankTrackingEntity> findByUserIdx(Integer userIdx);
    boolean existsByKeywordAndNvmidAndUserIdx(String keyword, String nvmid, Integer userIdx);
    int countByUserIdx(Integer userIdx);
}
