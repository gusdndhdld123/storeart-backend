package com.example.storeartbackend.Repository;

import com.example.storeartbackend.Entity.NaverSearchCountEntity;
import com.example.storeartbackend.Entity.SearchCountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NaverSearchCountRepository extends JpaRepository<NaverSearchCountEntity, Integer> {
    // 오늘 날짜와 userIdx로 검색 (없으면 기본값 삽입을 위해 Optional 사용)
    Optional<NaverSearchCountEntity> findByUserIdxAndDate(int userIdx, String date);

    // 오늘 날짜에 해당하는 사용자 검색 카운트 및 maxSearch 값 업데이트
    @Modifying
    @Query("UPDATE NaverSearchCountEntity sc SET sc.searchCount = :searchCount WHERE sc.userIdx = :userIdx AND sc.date = :date")
    void updateSearchCount(@Param("searchCount") int searchCount, @Param("userIdx") int userIdx, @Param("date") String date);

    // 새로운 기본값 생성 (없을 경우)
    @Modifying
    @Query("INSERT INTO NaverSearchCountEntity (userIdx, grade, searchCount, maxSearch, date) VALUES (:userIdx, :grade, 0, :maxSearch, :date)")
    void insertDefaultSearchCount(@Param("userIdx") int userIdx, @Param("grade") int grade, @Param("maxSearch") Integer maxSearch, @Param("date") String date);

    // userIdx와 오늘 날짜에 해당하는 maxSearch와 searchCount 조회
    @Query("SELECT sc FROM NaverSearchCountEntity sc WHERE sc.userIdx = :userIdx AND sc.date = :date")
    Optional<NaverSearchCountEntity> findSearchCountByUserIdxAndDate(@Param("userIdx") int userIdx, @Param("date") String date);

    List<SearchCountEntity> findByUserIdx(int userIdx);

    // userIdx와 현재 날짜 기준으로 가장 가까운 날짜를 찾는 쿼리
    @Query("SELECT s.date FROM NaverSearchCountEntity s WHERE s.userIdx = :userIdx AND s.date <= :currentDate ORDER BY s.date DESC")
    Optional<String> findClosestDate(@Param("userIdx") int userIdx, @Param("currentDate") String currentDate);
}
