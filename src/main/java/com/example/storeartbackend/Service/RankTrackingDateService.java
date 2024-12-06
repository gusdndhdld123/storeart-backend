package com.example.storeartbackend.Service;

import com.example.storeartbackend.Entity.RankTrackingDateEntity;
import com.example.storeartbackend.Repository.RankTrackingDateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RankTrackingDateService {

    private final RankTrackingDateRepository rankTrackingDateRepository;
    private final RankSearchService rankSearchService;

    // 1. 어제 날짜 데이터를 가져오기
    public List<RankTrackingDateEntity> getAllTrackingsFromYesterday() {
        LocalDate yesterday = LocalDate.now().minusDays(1); // 어제 날짜 계산
        return rankTrackingDateRepository.findByDate(yesterday); // 어제 데이터 조회
    }

    // 2. 키워드와 nvmid로 새로운 순위를 검색
    public int updateRank(String keyword, String nvmid) {
        return rankSearchService.searchRank(keyword, nvmid); // 외부 검색 서비스 호출
    }

    // 3. 새로운 순위를 현재 날짜로 저장
    public void saveDailyRank(Long trackingId, int rank, LocalDate date) {
        RankTrackingDateEntity rankTrackingDate = new RankTrackingDateEntity();
        rankTrackingDate.setRankTrackingIdx(Math.toIntExact(trackingId));
        rankTrackingDate.setRank(String.valueOf(rank));
        rankTrackingDate.setDate(date);

        rankTrackingDateRepository.save(rankTrackingDate); // DB에 저장
    }
}
