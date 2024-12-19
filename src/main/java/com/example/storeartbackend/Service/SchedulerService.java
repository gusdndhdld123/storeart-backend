package com.example.storeartbackend.Service;

import com.example.storeartbackend.Entity.DateLimitEntity;
import com.example.storeartbackend.Entity.RankTrackingDateEntity;
import com.example.storeartbackend.Entity.UserEntity;
import com.example.storeartbackend.Repository.DateLimitRepository;
import com.example.storeartbackend.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SchedulerService {


    private final RankTrackingDateService rankTrackingDateService;
    private final DateLimitService dateLimitService;
    private final DateLimitRepository dateLimitRepository;
    private final UserRepository userRepository;
    // 매일 오전 11시 00분에 실행
    @Scheduled(cron = "0 00 11 * * ?") // CRON 표현식 사용
    public void updateRankings() {
        System.out.println("[Scheduler] updateRankings 실행 시작: " + LocalDate.now());

        try {
            // 1. "어제 날짜"의 모든 RankTrackingDateEntity를 가져옴
            List<RankTrackingDateEntity> trackingList = rankTrackingDateService.getAllTrackingsFromYesterday();
            System.out.println("[Scheduler] 어제의 트래킹 데이터 개수: " + trackingList.size());

            // 2. 각 트래킹 데이터를 순회하면서 키워드와 nvmid로 새로운 순위를 검색 후 저장
            for (RankTrackingDateEntity tracking : trackingList) {
                System.out.println("[Scheduler] 처리 중 - ID: " + tracking.getRankTrackingIdx() +
                        ", 키워드: " + tracking.getKeyword() +
                        ", NVMID: " + tracking.getNvmid());

                // 2.1. 키워드와 nvmid로 새로운 순위 검색
                int rank = rankTrackingDateService.updateRank(tracking.getKeyword(), tracking.getNvmid());
                String keyword = tracking.getKeyword();
                String nvmid = tracking.getNvmid();
                System.out.println("[Scheduler] 새로운 순위: " + rank);

                // 2.2. "오늘 날짜"로 새 행 생성 및 저장
                rankTrackingDateService.saveDailyRank((long) tracking.getRankTrackingIdx(), rank, LocalDate.now(), nvmid, keyword);
                System.out.println("[Scheduler] 저장 완료 - ID: " + tracking.getRankTrackingIdx());
            }

        } catch (Exception e) {
            System.err.println("[Scheduler] 에러 발생: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("[Scheduler] updateRankings 실행 종료: " + LocalDate.now());
    }

    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정 실행
    @Transactional // 트랜잭션 처리
    public void updateDateLimit() {
        // 오늘 날짜 가져오기 (YYYY-MM-dd 형식)
        String today = LocalDate.now().toString();

        // endDate가 오늘 날짜와 같은 모든 데이터 가져오기
        List<DateLimitEntity> dateLimitEntities = dateLimitRepository.findAllByEndDate(today);

        if (!dateLimitEntities.isEmpty()) {
            for (DateLimitEntity dateLimitEntity : dateLimitEntities) {
                // userIdx로 UserEntity 검색
                int userIdx = dateLimitEntity.getUserIdx();
                UserEntity userEntity = userRepository.findById(userIdx)
                        .orElseThrow(() -> new RuntimeException("User not found with id: " + userIdx));

                // User의 grade를 "1"로 변경
                userEntity.setGrade("1");
                userRepository.save(userEntity);
            }
        }
    }
}
