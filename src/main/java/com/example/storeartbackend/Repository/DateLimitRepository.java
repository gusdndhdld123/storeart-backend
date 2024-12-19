package com.example.storeartbackend.Repository;

import com.example.storeartbackend.Entity.DateLimitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DateLimitRepository extends JpaRepository<DateLimitEntity, Long> {
    Optional<DateLimitEntity> findByUserIdx(int userIdx);
    // endDate가 주어진 날짜와 같은 데이터 검색
    List<DateLimitEntity> findAllByEndDate(String endDate);
    List<DateLimitEntity> findAllByUserIdx(int userIdx);
}
