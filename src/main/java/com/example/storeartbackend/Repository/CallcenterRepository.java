package com.example.storeartbackend.Repository;

import com.example.storeartbackend.Entity.CallcenterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface CallcenterRepository extends JpaRepository<CallcenterEntity,Long> {

    List<CallcenterEntity> findByUserIdx(int userIdx);
}
