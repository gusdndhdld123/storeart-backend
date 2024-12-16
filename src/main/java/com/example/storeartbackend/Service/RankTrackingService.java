package com.example.storeartbackend.Service;

import com.example.storeartbackend.DTO.RankTrackingDTO;
import com.example.storeartbackend.Entity.RankTrackingEntity;
import com.example.storeartbackend.Repository.RankTrackingDateRepository;
import com.example.storeartbackend.Repository.RankTrackingRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RankTrackingService {
    private final RankTrackingRepository rankTrackingRepository;
    private final RankTrackingDateRepository rankTrackingDateRepository;
    private final ModelMapper modelMapper;

    public RankTrackingDTO addOrUpdateRankTracking(RankTrackingDTO request) {
        // 기존 데이터 조회
        Optional<RankTrackingEntity> existingEntity = rankTrackingRepository.findByUserIdxAndKeywordAndNvmid(
                request.getUserIdx(), request.getKeyword(), request.getNvmid()
        );

        if (existingEntity.isPresent()) {
            // 기존 데이터가 존재하면 Entity를 DTO로 변환하여 반환
            return modelMapper.map(existingEntity.get(), RankTrackingDTO.class);
        } else {
            // 새 데이터 추가
            RankTrackingEntity newEntity = modelMapper.map(request, RankTrackingEntity.class);

            RankTrackingEntity savedEntity = rankTrackingRepository.save(newEntity);

            // 저장된 데이터를 DTO로 변환하여 반환
            return modelMapper.map(savedEntity, RankTrackingDTO.class);
        }
    }

    public void deleteTracking(Integer userIdx, String keyword, String nvmid) {
        // 1. RankTrackingEntity에서 데이터 조회
        Optional<RankTrackingEntity> trackingEntity = rankTrackingRepository.findByUserIdxAndKeywordAndNvmid(userIdx, keyword, nvmid);

        if (trackingEntity.isPresent()) {
            RankTrackingEntity entity = trackingEntity.get();

            // 2. RankTrackingEntity 삭제
            rankTrackingRepository.delete(entity);

            // 3. RankTrackingDateEntity에서 관련 데이터 삭제
            rankTrackingDateRepository.deleteByrankTrackingIdx(entity.getRankTrackingIdx());
        } else {
            throw new IllegalArgumentException("해당 키워드와 상품 ID에 대한 데이터가 존재하지 않습니다.");
        }
    }

    public List<RankTrackingDTO> getRankListByUserIdx(Integer userIdx) {
        // userIdx로 RankTrackingEntity 조회
        List<RankTrackingEntity> rankTrackingList = rankTrackingRepository.findByUserIdx(userIdx);

        // RankTrackingEntity를 RankTrackingDTO로 변환하여 리스트로 반환
        return rankTrackingList.stream()
                .map(entity -> modelMapper.map(entity, RankTrackingDTO.class))
                .collect(Collectors.toList());
    }
    public boolean existsByKeywordAndNvmidAndUserIdx(String keyword, String nvmid, Integer userIdx) {
        return rankTrackingRepository.existsByKeywordAndNvmidAndUserIdx(keyword, nvmid, userIdx); }


    // 등급별 maxCount 설정
    private static final Map<Integer, Integer> MAX_COUNT_BY_GRADE = new HashMap<>() {{
        put(0, 0);
        put(1, 0);
        put(2, 5);
        put(3, 10);
        put(4, 15);
        put(5, 20);
        put(6, 20);
    }};
    // nowCount와 maxCount 계산
    public Map<String, Integer> getSlotCounts(Integer userIdx, Integer grade) {
        if (grade < 0 || grade > 6) {
            throw new IllegalArgumentException("유효하지 않은 등급입니다.");
        }

        // nowCount 계산
        int nowCount = rankTrackingRepository.countByUserIdx(userIdx);

        // maxCount 가져오기
        int maxCount = MAX_COUNT_BY_GRADE.getOrDefault(grade, 0);

        // JSON 객체로 반환
        Map<String, Integer> result = new HashMap<>();
        result.put("nowCount", nowCount);
        result.put("maxCount", maxCount);

        return result;
    }
}
