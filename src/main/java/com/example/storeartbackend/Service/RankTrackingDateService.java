package com.example.storeartbackend.Service;

import com.example.storeartbackend.DTO.RankTrackingDateDTO;
import com.example.storeartbackend.Entity.RankTrackingDateEntity;
import com.example.storeartbackend.Repository.RankTrackingDateRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RankTrackingDateService {

    private final RankTrackingDateRepository rankTrackingDateRepository;
    private final RankSearchService rankSearchService;
    private final ModelMapper modelMapper;
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
    public void saveDailyRank(Long RankTrackingIdx, int rank, LocalDate date, String nvmid, String keyword) {
        RankTrackingDateEntity rankTrackingDate = new RankTrackingDateEntity();
        rankTrackingDate.setRankTrackingIdx(RankTrackingIdx);
        rankTrackingDate.setRank(String.valueOf(rank));
        rankTrackingDate.setDate(date);
        rankTrackingDate.setKeyword(keyword);
        rankTrackingDate.setNvmid(nvmid);

        rankTrackingDateRepository.save(rankTrackingDate); // DB에 저장
    }

    // 새로운 메서드: DTO를 매개변수로 받음
    public void saveDailyRank(RankTrackingDateDTO dateDTO) {
        RankTrackingDateEntity rankTrackingDate = new RankTrackingDateEntity();
        rankTrackingDate.setRankTrackingIdx((long) dateDTO.getRankTrackingIdx());
        rankTrackingDate.setRank(dateDTO.getRank());
        rankTrackingDate.setDate(dateDTO.getDate());
        rankTrackingDate.setKeyword(dateDTO.getKeyword());
        rankTrackingDate.setNvmid(dateDTO.getNvmid());

        rankTrackingDateRepository.save(rankTrackingDate); // DB에 저장
    }
    public List<RankTrackingDateDTO> getAllRankTrackingDates(Long rankTrackingIdx) {
        // RankTrackingIdx로 RankTrackingDateEntity 조회
        List<RankTrackingDateEntity> rankTrackingDateEntities = rankTrackingDateRepository.findByRankTrackingIdx(rankTrackingIdx);

        // ModelMapper로 RankTrackingDateEntity를 RankTrackingDateDTO로 변환하여 반환
        return rankTrackingDateEntities.stream()
                .map(entity -> modelMapper.map(entity, RankTrackingDateDTO.class))
                .collect(Collectors.toList());
    }
    // 새로운 메서드: RankTrackingIdx로 오브젝트 삭제
    public void deleteAllByRankTrackingIdx(Long rankTrackingIdx) {
        List<RankTrackingDateEntity> rankTrackingDateEntities = rankTrackingDateRepository.findByRankTrackingIdx(rankTrackingIdx);
        rankTrackingDateRepository.deleteAll(rankTrackingDateEntities);
    }
}
