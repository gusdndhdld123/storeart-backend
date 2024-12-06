package com.example.storeartbackend.Service;

import com.example.storeartbackend.Entity.SearchCountEntity;
import com.example.storeartbackend.Repository.SearchcountRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class SearchcountService {

    @Autowired
    private SearchcountRepository searchcountRepository;

    // 오늘 날짜를 반환
    private String getTodayDate() {
        return LocalDate.now().format(DateTimeFormatter.ISO_DATE);  // yyyy-MM-dd 포맷
    }


    // 오늘 날짜의 검색 횟수 확인 및 처리
    @Transactional
    public void handleSearch(int userIdx) {
        String today = getTodayDate();

        // 오늘 날짜와 userIdx로 검색
        Optional<SearchCountEntity> searchcountOpt = searchcountRepository.findByUserIdxAndDate(userIdx, today);

        // 데이터가 없으면 기본값 생성
        if (!searchcountOpt.isPresent()) {
            // 기본값 생성: userIdx에 맞는 기본 grade와 maxSearch 삽입
            int grade = 1;  // 기본값 (무료 유저)
            int maxSearch = 30;  // 기본 maxSearch 값

            searchcountRepository.insertDefaultSearchCount(userIdx, grade, maxSearch, today);
        } else {
            // 데이터가 있으면 검색횟수와 maxSearch 값을 비교
            SearchCountEntity searchcount = searchcountOpt.get();
            int searchCount = searchcount.getSearchCount();
            Integer maxSearch = searchcount.getMaxSearch();

            // maxSearch가 null인 경우 제한 없음, searchCount가 maxSearch와 같으면 alert 처리
            if (searchCount >= (maxSearch != null ? maxSearch : Integer.MAX_VALUE)) {
                System.out.println("오늘의 검색량을 모두 소진하였습니다.");
            } else {
                // searchCount 증가 후 저장
                searchcount.setSearchCount(searchCount + 1);
                searchcountRepository.updateSearchCount(searchcount.getSearchCount(), userIdx, today);
            }
        }
    }
    @Transactional
    public void insertDefaultSearchCount(int userIdx, int grade, Integer maxSearch, String date) {

        SearchCountEntity entity = new SearchCountEntity();
        entity.setUserIdx(userIdx);
        entity.setGrade(grade);
        entity.setSearchCount(0);
        entity.setMaxSearch(maxSearch);
        entity.setDate(date);
        searchcountRepository.save(entity);
    }
    public void plusSearchCount(int userIdx) {
        // 오늘 날짜를 가져옴
        String todayDate = getTodayDate();

        // userIdx와 오늘 날짜로 SearchCountEntity 찾기
        Optional<SearchCountEntity> nowsearch = searchcountRepository.findByUserIdxAndDate(userIdx, todayDate);

        // 해당 데이터가 존재하면, 검색 횟수 증가
        nowsearch.ifPresentOrElse(
                entity -> {
                    entity.setSearchCount(entity.getSearchCount() + 1);  // 검색 횟수 +1
                    searchcountRepository.save(entity);  // 수정된 엔티티 저장
                },
                () -> {
                    // 해당 날짜의 데이터가 없으면 예외를 던지거나 추가적인 처리
                    throw new RuntimeException("No data found for the userIdx and date");
                }
        );
    }



    public SearchCountEntity getSearchCount(int userIdx, LocalDate currentDate) {
        // 현재 날짜를 "yyyy-MM-dd" 형식으로 변환
        String currentDateString = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // userIdx와 currentDate로 검색
        return searchcountRepository.findSearchCountByUserIdxAndDate(userIdx, currentDateString)
                .orElseGet(() -> {
                    // 해당 날짜의 데이터가 없으면, 가장 가까운 날짜의 데이터를 찾기
                    String closestDateString = searchcountRepository.findClosestDate(userIdx, currentDateString)
                            .orElseThrow(() -> new RuntimeException("No previous data found for the userIdx"));

                    // 가장 가까운 날짜의 데이터를 가져와서 새로운 객체를 생성
                    SearchCountEntity closestData = searchcountRepository.findSearchCountByUserIdxAndDate(userIdx, closestDateString)
                            .orElseThrow(() -> new RuntimeException("No data found for the closest date"));

                    // 새로운 객체 생성
                    SearchCountEntity newEntity = new SearchCountEntity();
                    newEntity.setUserIdx(userIdx);
                    newEntity.setDate(currentDateString);
                    newEntity.setSearchCount(0); // SearchCount는 0으로 설정

                    // 기존 데이터를 복사 (필요한 필드만)
                    newEntity.setGrade(closestData.getGrade());    // grade 복사
                    newEntity.setMaxSearch(closestData.getMaxSearch());  // maxSearch 복사
                    // 다른 필요한 필드들도 복사할 수 있습니다.

                    // 새 데이터를 저장
                    searchcountRepository.save(newEntity);

                    return newEntity;
                });
    }
    //maxcount를 변경

    public void handlegrade1(int userIdx, LocalDate currentDate) {
        // 현재 날짜를 "yyyy-MM-dd" 형식으로 변환
        String currentDateString = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // userIdx와 currentDate로 검색
        Optional<SearchCountEntity> searchCountEntityOptional = searchcountRepository.findSearchCountByUserIdxAndDate(userIdx, currentDateString);

        if (searchCountEntityOptional.isPresent()) {
            // 데이터가 존재하면, 해당 엔티티를 불러와서 maxSearch를 30으로 변경
            SearchCountEntity existingEntity = searchCountEntityOptional.get();
            existingEntity.setMaxSearch(5);
            searchcountRepository.save(existingEntity);
        } else {
            // 데이터가 없으면, 오늘 날짜로 새 엔티티 생성 후 maxSearch를 30으로 설정하여 저장
            SearchCountEntity newEntity = new SearchCountEntity();
            newEntity.setUserIdx(userIdx);
            newEntity.setDate(currentDateString);
            newEntity.setMaxSearch(5); // maxSearch를 30으로 설정

            // 필요한 다른 필드를 설정
            newEntity.setSearchCount(0);  // 예시로 searchCount는 0으로 설정 (필요에 맞게 조정)

            searchcountRepository.save(newEntity);
        }
    }
    public void handlegrade2(int userIdx, LocalDate currentDate) {
        // 현재 날짜를 "yyyy-MM-dd" 형식으로 변환
        String currentDateString = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // userIdx와 currentDate로 검색
        Optional<SearchCountEntity> searchCountEntityOptional = searchcountRepository.findSearchCountByUserIdxAndDate(userIdx, currentDateString);

        if (searchCountEntityOptional.isPresent()) {
            // 데이터가 존재하면, 해당 엔티티를 불러와서 maxSearch를 30으로 변경
            SearchCountEntity existingEntity = searchCountEntityOptional.get();
            existingEntity.setMaxSearch(10);
            searchcountRepository.save(existingEntity);
        } else {
            // 데이터가 없으면, 오늘 날짜로 새 엔티티 생성 후 maxSearch를 30으로 설정하여 저장
            SearchCountEntity newEntity = new SearchCountEntity();
            newEntity.setUserIdx(userIdx);
            newEntity.setDate(currentDateString);
            newEntity.setMaxSearch(10); // maxSearch를 30으로 설정

            // 필요한 다른 필드를 설정
            newEntity.setSearchCount(0);  // 예시로 searchCount는 0으로 설정 (필요에 맞게 조정)

            searchcountRepository.save(newEntity);
        }
    }
    public void handlegrade3(int userIdx, LocalDate currentDate) {
        // 현재 날짜를 "yyyy-MM-dd" 형식으로 변환
        String currentDateString = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // userIdx와 currentDate로 검색
        Optional<SearchCountEntity> searchCountEntityOptional = searchcountRepository.findSearchCountByUserIdxAndDate(userIdx, currentDateString);

        if (searchCountEntityOptional.isPresent()) {
            // 데이터가 존재하면, 해당 엔티티를 불러와서 maxSearch를 30으로 변경
            SearchCountEntity existingEntity = searchCountEntityOptional.get();
            existingEntity.setMaxSearch(20);
            searchcountRepository.save(existingEntity);
        } else {
            // 데이터가 없으면, 오늘 날짜로 새 엔티티 생성 후 maxSearch를 30으로 설정하여 저장
            SearchCountEntity newEntity = new SearchCountEntity();
            newEntity.setUserIdx(userIdx);
            newEntity.setDate(currentDateString);
            newEntity.setMaxSearch(20); // maxSearch를 30으로 설정

            // 필요한 다른 필드를 설정
            newEntity.setSearchCount(0);  // 예시로 searchCount는 0으로 설정 (필요에 맞게 조정)

            searchcountRepository.save(newEntity);
        }
    }
    public void handlegrade4(int userIdx, LocalDate currentDate) {
        // 현재 날짜를 "yyyy-MM-dd" 형식으로 변환
        String currentDateString = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // userIdx와 currentDate로 검색
        Optional<SearchCountEntity> searchCountEntityOptional = searchcountRepository.findSearchCountByUserIdxAndDate(userIdx, currentDateString);

        if (searchCountEntityOptional.isPresent()) {
            // 데이터가 존재하면, 해당 엔티티를 불러와서 maxSearch를 30으로 변경
            SearchCountEntity existingEntity = searchCountEntityOptional.get();
            existingEntity.setMaxSearch(30);
            searchcountRepository.save(existingEntity);
        } else {
            // 데이터가 없으면, 오늘 날짜로 새 엔티티 생성 후 maxSearch를 30으로 설정하여 저장
            SearchCountEntity newEntity = new SearchCountEntity();
            newEntity.setUserIdx(userIdx);
            newEntity.setDate(currentDateString);
            newEntity.setMaxSearch(30); // maxSearch를 30으로 설정

            // 필요한 다른 필드를 설정
            newEntity.setSearchCount(0);  // 예시로 searchCount는 0으로 설정 (필요에 맞게 조정)

            searchcountRepository.save(newEntity);
        }
    }
    public void handlegrade5(int userIdx, LocalDate currentDate) {
        // 현재 날짜를 "yyyy-MM-dd" 형식으로 변환
        String currentDateString = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // userIdx와 currentDate로 검색
        Optional<SearchCountEntity> searchCountEntityOptional = searchcountRepository.findSearchCountByUserIdxAndDate(userIdx, currentDateString);

        if (searchCountEntityOptional.isPresent()) {
            // 데이터가 존재하면, 해당 엔티티를 불러와서 maxSearch를 30으로 변경
            SearchCountEntity existingEntity = searchCountEntityOptional.get();
            existingEntity.setMaxSearch(50);
            searchcountRepository.save(existingEntity);
        } else {
            // 데이터가 없으면, 오늘 날짜로 새 엔티티 생성 후 maxSearch를 30으로 설정하여 저장
            SearchCountEntity newEntity = new SearchCountEntity();
            newEntity.setUserIdx(userIdx);
            newEntity.setDate(currentDateString);
            newEntity.setMaxSearch(50); // maxSearch를 30으로 설정

            // 필요한 다른 필드를 설정
            newEntity.setSearchCount(0);  // 예시로 searchCount는 0으로 설정 (필요에 맞게 조정)

            searchcountRepository.save(newEntity);
        }
    }
    public void handlegrade6(int userIdx, LocalDate currentDate) {
        // 현재 날짜를 "yyyy-MM-dd" 형식으로 변환
        String currentDateString = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // userIdx와 currentDate로 검색
        Optional<SearchCountEntity> searchCountEntityOptional = searchcountRepository.findSearchCountByUserIdxAndDate(userIdx, currentDateString);

        if (searchCountEntityOptional.isPresent()) {
            // 데이터가 존재하면, 해당 엔티티를 불러와서 maxSearch를 30으로 변경
            SearchCountEntity existingEntity = searchCountEntityOptional.get();
            existingEntity.setMaxSearch(10000);
            searchcountRepository.save(existingEntity);
        } else {
            // 데이터가 없으면, 오늘 날짜로 새 엔티티 생성 후 maxSearch를 30으로 설정하여 저장
            SearchCountEntity newEntity = new SearchCountEntity();
            newEntity.setUserIdx(userIdx);
            newEntity.setDate(currentDateString);
            newEntity.setMaxSearch(10000); // maxSearch를 30으로 설정

            // 필요한 다른 필드를 설정
            newEntity.setSearchCount(0);  // 예시로 searchCount는 0으로 설정 (필요에 맞게 조정)

            searchcountRepository.save(newEntity);
        }
    }

}