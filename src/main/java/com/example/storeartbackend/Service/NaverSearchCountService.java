package com.example.storeartbackend.Service;

import com.example.storeartbackend.Entity.NaverSearchCountEntity;
import com.example.storeartbackend.Entity.SearchCountEntity;
import com.example.storeartbackend.Repository.NaverSearchCountRepository;
import com.example.storeartbackend.Repository.SearchcountRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
@Service
public class NaverSearchCountService {
    @Autowired
    private NaverSearchCountRepository naverSearchCountRepository;

    // 오늘 날짜를 반환
    private String getTodayDate() {
        return LocalDate.now().format(DateTimeFormatter.ISO_DATE);  // yyyy-MM-dd 포맷
    }


    // 오늘 날짜의 검색 횟수 확인 및 처리
    @Transactional
    public void naverhandleSearch(int userIdx) {
        String today = getTodayDate();

        // 오늘 날짜와 userIdx로 검색
        Optional<NaverSearchCountEntity> searchcountOpt = naverSearchCountRepository.findByUserIdxAndDate(userIdx, today);

        // 데이터가 없으면 기본값 생성
        if (!searchcountOpt.isPresent()) {
            // 기본값 생성: userIdx에 맞는 기본 grade와 maxSearch 삽입
            int grade = 1;  // 기본값 (무료 유저)
            int maxSearch = 30;  // 기본 maxSearch 값

            naverSearchCountRepository.insertDefaultSearchCount(userIdx, grade, maxSearch, today);
        } else {
            // 데이터가 있으면 검색횟수와 maxSearch 값을 비교
            NaverSearchCountEntity naversearchcount = searchcountOpt.get();
            int searchCount = naversearchcount.getSearchCount();
            Integer maxSearch = naversearchcount.getMaxSearch();

            // maxSearch가 null인 경우 제한 없음, searchCount가 maxSearch와 같으면 alert 처리
            if (searchCount >= (maxSearch != null ? maxSearch : Integer.MAX_VALUE)) {

            } else {
                // searchCount 증가 후 저장
                naversearchcount.setSearchCount(searchCount + 1);
                naverSearchCountRepository.updateSearchCount(naversearchcount.getSearchCount(), userIdx, today);
            }
        }
    }
    @Transactional
    public void insertDefaultSearchCount(int userIdx, int grade, Integer maxSearch, String date) {

        NaverSearchCountEntity entity = new NaverSearchCountEntity();
        entity.setUserIdx(userIdx);
        entity.setGrade(grade);
        entity.setSearchCount(0);
        entity.setMaxSearch(maxSearch);
        entity.setDate(date);
        naverSearchCountRepository.save(entity);
    }
    public void plusSearchCount(int userIdx) {
        // 오늘 날짜를 가져옴
        String todayDate = getTodayDate();

        // userIdx와 오늘 날짜로 SearchCountEntity 찾기
        Optional<NaverSearchCountEntity> nowsearch = naverSearchCountRepository.findByUserIdxAndDate(userIdx, todayDate);

        // 해당 데이터가 존재하면, 검색 횟수 증가
        nowsearch.ifPresentOrElse(
                entity -> {
                    entity.setSearchCount(entity.getSearchCount() + 1);  // 검색 횟수 +1
                    naverSearchCountRepository.save(entity);  // 수정된 엔티티 저장
                },
                () -> {
                    // 해당 날짜의 데이터가 없으면 예외를 던지거나 추가적인 처리
                    throw new RuntimeException("No data found for the userIdx and date");
                }
        );
    }



    @Transactional
    public NaverSearchCountEntity getSearchCount(int userIdx, LocalDate currentDate) {
        // 현재 날짜를 "yyyy-MM-dd" 형식으로 변환
        String currentDateString = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // userIdx와 currentDate로 검색
        Optional<NaverSearchCountEntity> searchCount = naverSearchCountRepository.findSearchCountByUserIdxAndDate(userIdx, currentDateString);

        if (searchCount.isPresent()) {
            return searchCount.get(); // 데이터가 있으면 바로 반환
        }

        // 해당 날짜의 데이터가 없으면, 가장 가까운 날짜의 데이터를 찾기
        String closestDateString = naverSearchCountRepository.findClosestDate(userIdx, currentDateString)
                .orElse(currentDateString); // 가장 가까운 데이터가 없으면 현재 날짜를 사용

        // 가장 가까운 날짜의 데이터를 가져와서 새로운 객체를 생성
        NaverSearchCountEntity closestData = naverSearchCountRepository.findSearchCountByUserIdxAndDate(userIdx, closestDateString)
                .orElseGet(() -> {
                    // 가장 가까운 날짜 데이터도 없다면 새 데이터를 기본값으로 생성하여 반환
                    Integer maxSearch = 5; // 기본값
                    int grade = 1; // 기본값

                    // 새 엔티티를 생성하여 반환
                    NaverSearchCountEntity newEntity = new NaverSearchCountEntity();
                    newEntity.setUserIdx(userIdx);
                    newEntity.setDate(closestDateString);
                    newEntity.setSearchCount(0); // 기본값 설정
                    newEntity.setGrade(grade); // 기본 grade 설정
                    newEntity.setMaxSearch(maxSearch); // 기본 maxSearch 설정

                    // 데이터베이스에 저장하여 반환
                    return naverSearchCountRepository.save(newEntity);
                });

        // 새 데이터를 생성하여 반환
        NaverSearchCountEntity newEntity = new NaverSearchCountEntity();
        newEntity.setUserIdx(userIdx);
        newEntity.setDate(currentDateString);
        newEntity.setSearchCount(0); // 기본값 설정
        newEntity.setGrade(closestData.getGrade()); // 가장 가까운 데이터의 grade 복사
        newEntity.setMaxSearch(closestData.getMaxSearch()); // 가장 가까운 데이터의 maxSearch 복사

        // 새 데이터를 저장
        return naverSearchCountRepository.save(newEntity);
    }
    public void handlegrade0(int userIdx, LocalDate currentDate) {
        // 현재 날짜를 "yyyy-MM-dd" 형식으로 변환
        String currentDateString = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // userIdx와 currentDate로 검색
        Optional<NaverSearchCountEntity> searchCountEntityOptional = naverSearchCountRepository.findSearchCountByUserIdxAndDate(userIdx, currentDateString);

        if (searchCountEntityOptional.isPresent()) {
            // 데이터가 존재하면, 해당 엔티티를 불러와서 maxSearch를 30으로 변경
            NaverSearchCountEntity existingEntity = searchCountEntityOptional.get();
            existingEntity.setMaxSearch(0);
            naverSearchCountRepository.save(existingEntity);
        } else {
            // 데이터가 없으면, 오늘 날짜로 새 엔티티 생성 후 maxSearch를 30으로 설정하여 저장
            NaverSearchCountEntity newEntity = new NaverSearchCountEntity();
            newEntity.setUserIdx(userIdx);
            newEntity.setDate(currentDateString);
            newEntity.setMaxSearch(0); // maxSearch를 30으로 설정

            // 필요한 다른 필드를 설정
            newEntity.setSearchCount(0);  // 예시로 searchCount는 0으로 설정 (필요에 맞게 조정)

            naverSearchCountRepository.save(newEntity);
        }
    }
    public void handlegrade1(int userIdx, LocalDate currentDate) {
        // 현재 날짜를 "yyyy-MM-dd" 형식으로 변환
        String currentDateString = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // userIdx와 currentDate로 검색
        Optional<NaverSearchCountEntity> searchCountEntityOptional = naverSearchCountRepository.findSearchCountByUserIdxAndDate(userIdx, currentDateString);

        if (searchCountEntityOptional.isPresent()) {
            // 데이터가 존재하면, 해당 엔티티를 불러와서 maxSearch를 30으로 변경
            NaverSearchCountEntity existingEntity = searchCountEntityOptional.get();
            existingEntity.setMaxSearch(3);
            naverSearchCountRepository.save(existingEntity);
        } else {
            // 데이터가 없으면, 오늘 날짜로 새 엔티티 생성 후 maxSearch를 30으로 설정하여 저장
            NaverSearchCountEntity newEntity = new NaverSearchCountEntity();
            newEntity.setUserIdx(userIdx);
            newEntity.setDate(currentDateString);
            newEntity.setMaxSearch(3); // maxSearch를 30으로 설정

            // 필요한 다른 필드를 설정
            newEntity.setSearchCount(0);  // 예시로 searchCount는 0으로 설정 (필요에 맞게 조정)

            naverSearchCountRepository.save(newEntity);
        }
    }
    public void handlegrade2(int userIdx, LocalDate currentDate) {
        // 현재 날짜를 "yyyy-MM-dd" 형식으로 변환
        String currentDateString = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // userIdx와 currentDate로 검색
        Optional<NaverSearchCountEntity> searchCountEntityOptional = naverSearchCountRepository.findSearchCountByUserIdxAndDate(userIdx, currentDateString);

        if (searchCountEntityOptional.isPresent()) {
            // 데이터가 존재하면, 해당 엔티티를 불러와서 maxSearch를 30으로 변경
            NaverSearchCountEntity existingEntity = searchCountEntityOptional.get();
            existingEntity.setMaxSearch(10);
            naverSearchCountRepository.save(existingEntity);
        } else {
            // 데이터가 없으면, 오늘 날짜로 새 엔티티 생성 후 maxSearch를 30으로 설정하여 저장
            NaverSearchCountEntity newEntity = new NaverSearchCountEntity();
            newEntity.setUserIdx(userIdx);
            newEntity.setDate(currentDateString);
            newEntity.setMaxSearch(10); // maxSearch를 30으로 설정

            // 필요한 다른 필드를 설정
            newEntity.setSearchCount(0);  // 예시로 searchCount는 0으로 설정 (필요에 맞게 조정)

            naverSearchCountRepository.save(newEntity);
        }
    }
    public void handlegrade3(int userIdx, LocalDate currentDate) {
        // 현재 날짜를 "yyyy-MM-dd" 형식으로 변환
        String currentDateString = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // userIdx와 currentDate로 검색
        Optional<NaverSearchCountEntity> searchCountEntityOptional = naverSearchCountRepository.findSearchCountByUserIdxAndDate(userIdx, currentDateString);

        if (searchCountEntityOptional.isPresent()) {
            // 데이터가 존재하면, 해당 엔티티를 불러와서 maxSearch를 30으로 변경
            NaverSearchCountEntity existingEntity = searchCountEntityOptional.get();
            existingEntity.setMaxSearch(20);
            naverSearchCountRepository.save(existingEntity);
        } else {
            // 데이터가 없으면, 오늘 날짜로 새 엔티티 생성 후 maxSearch를 30으로 설정하여 저장
            NaverSearchCountEntity newEntity = new NaverSearchCountEntity();
            newEntity.setUserIdx(userIdx);
            newEntity.setDate(currentDateString);
            newEntity.setMaxSearch(20); // maxSearch를 30으로 설정

            // 필요한 다른 필드를 설정
            newEntity.setSearchCount(0);  // 예시로 searchCount는 0으로 설정 (필요에 맞게 조정)

            naverSearchCountRepository.save(newEntity);
        }
    }
    public void handlegrade4(int userIdx, LocalDate currentDate) {
        // 현재 날짜를 "yyyy-MM-dd" 형식으로 변환
        String currentDateString = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // userIdx와 currentDate로 검색
        Optional<NaverSearchCountEntity> searchCountEntityOptional = naverSearchCountRepository.findSearchCountByUserIdxAndDate(userIdx, currentDateString);

        if (searchCountEntityOptional.isPresent()) {
            // 데이터가 존재하면, 해당 엔티티를 불러와서 maxSearch를 30으로 변경
            NaverSearchCountEntity existingEntity = searchCountEntityOptional.get();
            existingEntity.setMaxSearch(30);
            naverSearchCountRepository.save(existingEntity);
        } else {
            // 데이터가 없으면, 오늘 날짜로 새 엔티티 생성 후 maxSearch를 30으로 설정하여 저장
            NaverSearchCountEntity newEntity = new NaverSearchCountEntity();
            newEntity.setUserIdx(userIdx);
            newEntity.setDate(currentDateString);
            newEntity.setMaxSearch(30); // maxSearch를 30으로 설정

            // 필요한 다른 필드를 설정
            newEntity.setSearchCount(0);  // 예시로 searchCount는 0으로 설정 (필요에 맞게 조정)

            naverSearchCountRepository.save(newEntity);
        }
    }
    public void handlegrade5(int userIdx, LocalDate currentDate) {
        // 현재 날짜를 "yyyy-MM-dd" 형식으로 변환
        String currentDateString = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // userIdx와 currentDate로 검색
        Optional<NaverSearchCountEntity> searchCountEntityOptional = naverSearchCountRepository.findSearchCountByUserIdxAndDate(userIdx, currentDateString);

        if (searchCountEntityOptional.isPresent()) {
            // 데이터가 존재하면, 해당 엔티티를 불러와서 maxSearch를 30으로 변경
            NaverSearchCountEntity existingEntity = searchCountEntityOptional.get();
            existingEntity.setMaxSearch(50);
            naverSearchCountRepository.save(existingEntity);
        } else {
            // 데이터가 없으면, 오늘 날짜로 새 엔티티 생성 후 maxSearch를 30으로 설정하여 저장
            NaverSearchCountEntity newEntity = new NaverSearchCountEntity();
            newEntity.setUserIdx(userIdx);
            newEntity.setDate(currentDateString);
            newEntity.setMaxSearch(50); // maxSearch를 30으로 설정

            // 필요한 다른 필드를 설정
            newEntity.setSearchCount(0);  // 예시로 searchCount는 0으로 설정 (필요에 맞게 조정)

            naverSearchCountRepository.save(newEntity);
        }
    }
    public void handlegrade6(int userIdx, LocalDate currentDate) {
        // 현재 날짜를 "yyyy-MM-dd" 형식으로 변환
        String currentDateString = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // userIdx와 currentDate로 검색
        Optional<NaverSearchCountEntity> searchCountEntityOptional = naverSearchCountRepository.findSearchCountByUserIdxAndDate(userIdx, currentDateString);

        if (searchCountEntityOptional.isPresent()) {
            // 데이터가 존재하면, 해당 엔티티를 불러와서 maxSearch를 30으로 변경
            NaverSearchCountEntity existingEntity = searchCountEntityOptional.get();
            existingEntity.setMaxSearch(10000);
            naverSearchCountRepository.save(existingEntity);
        } else {
            // 데이터가 없으면, 오늘 날짜로 새 엔티티 생성 후 maxSearch를 30으로 설정하여 저장
            NaverSearchCountEntity newEntity = new NaverSearchCountEntity();
            newEntity.setUserIdx(userIdx);
            newEntity.setDate(currentDateString);
            newEntity.setMaxSearch(10000); // maxSearch를 30으로 설정

            // 필요한 다른 필드를 설정
            newEntity.setSearchCount(0);  // 예시로 searchCount는 0으로 설정 (필요에 맞게 조정)

            naverSearchCountRepository.save(newEntity);
        }
    }
}
