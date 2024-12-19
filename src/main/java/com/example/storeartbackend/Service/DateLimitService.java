package com.example.storeartbackend.Service;

import com.example.storeartbackend.DTO.DateLimitDTO;
import com.example.storeartbackend.Entity.DateLimitEntity;
import com.example.storeartbackend.Repository.DateLimitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DateLimitService {
    private final DateLimitRepository dateLimitRepository;
    private final ModelMapper modelMapper;

    // Create: 데이터 저장
    public void saveDateLimit(DateLimitDTO dateLimitDTO) {
        // DTO를 Entity로 변환
        DateLimitEntity dateLimitEntity = modelMapper.map(dateLimitDTO, DateLimitEntity.class);

        // Entity를 저장
        dateLimitRepository.save(dateLimitEntity);
    }

    // Read: ID로 데이터 조회
    public DateLimitDTO getDateLimitById(Long id) {
        // ID로 Entity 조회
        Optional<DateLimitEntity> dateLimitEntity = dateLimitRepository.findById(id);

        // Entity를 DTO로 변환 후 반환
        return dateLimitEntity.map(entity -> modelMapper.map(entity, DateLimitDTO.class))
                .orElse(null);
    }
    public DateLimitDTO getDateLimitByuserIdx(int userIdx){
        Optional<DateLimitEntity> dateLimitEntity = dateLimitRepository.findByUserIdx(userIdx);
        return dateLimitEntity.map(entity -> modelMapper.map(entity, DateLimitDTO.class))
                .orElse(null);
    }

    // Read: 모든 데이터 조회
    public List<DateLimitDTO> getAllDateLimits() {
        // 모든 Entity를 조회 후 DTO로 변환
        List<DateLimitEntity> dateLimits = dateLimitRepository.findAll();

        // Entity 리스트 -> DTO 리스트 변환
        return dateLimits.stream()
                .map(entity -> modelMapper.map(entity, DateLimitDTO.class))
                .toList();
    }

    // Update: 데이터 수정
    public void updateDateLimit(Long id, DateLimitDTO dateLimitDTO) {
        // 존재하는 데이터 확인
        Optional<DateLimitEntity> existingEntity = dateLimitRepository.findById(id);

        if (existingEntity.isPresent()) {
            // 기존 Entity를 DTO 내용으로 업데이트
            DateLimitEntity dateLimitEntity = existingEntity.get();
            dateLimitEntity.setStartDate(dateLimitDTO.getStartDate());
            dateLimitEntity.setEndDate(dateLimitDTO.getEndDate());
            dateLimitEntity.setUserIdx(dateLimitDTO.getUserIdx());

            // 업데이트된 Entity 저장
            dateLimitRepository.save(dateLimitEntity);
        } else {
            throw new RuntimeException("Entity with ID " + id + " not found!");
        }
    }

    // Delete: 데이터 삭제
    public void deleteDateLimit(Long id) {
        // 존재하는 데이터 확인 후 삭제
        if (dateLimitRepository.existsById(id)) {
            dateLimitRepository.deleteById(id);
        } else {
            throw new RuntimeException("Entity with ID " + id + " not found!");
        }
    }
    public void deleteDateLimitsByUserIdx(int userIdx) {
        List<DateLimitEntity> dateLimits = dateLimitRepository.findAllByUserIdx(userIdx);
        if (dateLimits.isEmpty()) {
            // 데이터가 없으면 로그만 남기고 처리 종료
            System.out.println("No subscription found for userIdx: " + userIdx);
            return;
        }
        dateLimitRepository.deleteAll(dateLimits);
    }

    public int calculateRemainingDays(int userIdx) {
        // 날짜 포맷 (DateLimitEntity의 endDate가 String으로 저장되어 있음)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        System.out.println("남은  날짜 계산 서비스까지 옴");
        // userIdx로 데이터 검색
        Optional<DateLimitEntity> dateLimitEntityOptional = dateLimitRepository.findByUserIdx(userIdx);

        // 데이터가 없는 경우 무료 유저 (-1 반환)
        if (dateLimitEntityOptional.isEmpty()) {
            return -1; // 무료 유저
        }
        
        // 유료 유저라면 endDate를 확인
        DateLimitEntity dateLimitEntity = dateLimitEntityOptional.get();
        String endDateString = dateLimitEntity.getEndDate(); // endDate 가져오기
        LocalDate endDate = LocalDate.parse(endDateString, formatter); // String -> LocalDate 변환
        LocalDate today = LocalDate.now(); // 오늘 날짜
        
        // 오늘 날짜와 endDate 비교
        if (today.isAfter(endDate)) {
            // 구독 만료 상태 (endDate < today인 경우)
            return 0; // 구독 기간 만료
        }

        // 남은 날짜 계산
        return (int) ChronoUnit.DAYS.between(today, endDate);
    }
}