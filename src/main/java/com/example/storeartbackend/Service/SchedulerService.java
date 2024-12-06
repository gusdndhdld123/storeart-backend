package com.example.storeartbackend.Service;

import com.example.storeartbackend.Entity.RankTrackingDateEntity;
import com.example.storeartbackend.Entity.RankTrackingEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class SchedulerService {

    @Autowired
    private RankTrackingDateService rankTrackingDateService;

    // @Scheduled 애노테이션: 24시간마다 실행
    @Scheduled(fixedRate = 86400000) // 밀리초 단위 (24시간)
    public void updateRankings() {
        // 1. "어제 날짜"의 모든 RankTrackingDateEntity를 가져옴
        List<RankTrackingDateEntity> trackingList = rankTrackingDateService.getAllTrackingsFromYesterday();

        // 2. 각 트래킹 데이터를 순회하면서 키워드와 nvmid로 새로운 순위를 검색 후 저장
        for (RankTrackingDateEntity tracking : trackingList) {
            // 2.1. 키워드와 nvmid로 새로운 순위 검색
            int rank = rankTrackingDateService.updateRank(tracking.getKeyword(), tracking.getNvmid());

            // 2.2. "오늘 날짜"로 새 행 생성 및 저장
            rankTrackingDateService.saveDailyRank((long) tracking.getRankTrackingIdx(), rank, LocalDate.now());
        }
    }
}