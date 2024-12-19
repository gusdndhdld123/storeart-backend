package com.example.storeartbackend.Controller;

import com.example.storeartbackend.DTO.RankTrackingDTO;
import com.example.storeartbackend.DTO.RankTrackingDateDTO;
import com.example.storeartbackend.Entity.RankTrackingEntity;
import com.example.storeartbackend.Service.RankSearchService;
import com.example.storeartbackend.Service.RankTrackingDateService;
import com.example.storeartbackend.Service.RankTrackingService;
import com.example.storeartbackend.Service.UserService;
import com.example.storeartbackend.Util.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sound.midi.Soundbank;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/rank")
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class RankTrackingController {
    private final RankTrackingService rankTrackingService;
    private final RankTrackingDateService rankTrackingDateService;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RankSearchService rankSearchService;

    //    진짜로 쓸 놈
    @PostMapping("/register")
    public ResponseEntity<?> registerTracking(@RequestBody RankTrackingDTO request, @RequestHeader("Authorization") String token) {

        try {
            // 1. DTO에서 데이터 추출
            String keyword = request.getKeyword();
            String nvmid = request.getNvmid();

            // 2. 토큰으로 userIdx 추출
            Claims claims = jwtTokenProvider.parseClaims(token.replace("Bearer ", "")); // "Bearer " 제거
            Integer userIdx = claims.get("userIdx", Integer.class);
            System.out.println(userIdx);
            if (userIdx == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            request.setUserIdx(userIdx);
            // 3. keyword, nvmid, userIdx로 기존 트랙킹 확인
            boolean exists = rankTrackingService.existsByKeywordAndNvmidAndUserIdx(keyword, nvmid, userIdx);
            if (exists) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "이미 존재하는 트랙킹입니다."));
            }

            // 4. 순위 검색
            int rank = rankSearchService.searchRank(keyword, nvmid);

            // 5. 저장 처리
            RankTrackingDTO trackingDTO = rankTrackingService.addOrUpdateRankTracking(request);

            RankTrackingDateDTO dateDTO = new RankTrackingDateDTO();
            dateDTO.setRankTrackingIdx(trackingDTO.getRankTrackingIdx().intValue());
            dateDTO.setKeyword(keyword);
            dateDTO.setNvmid(nvmid);
            dateDTO.setRank(String.valueOf(rank));
            dateDTO.setDate(LocalDate.now());

            rankTrackingDateService.saveDailyRank(dateDTO);

            return ResponseEntity.ok(Map.of("message", "순위 추적이 성공적으로 등록되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    //테스트용
//    @PostMapping("/register")
//    public ResponseEntity<?> registerTracking(@RequestBody RankTrackingDTO request) {
//
//        try {
//            // 1. DTO에서 데이터 추출
//            String keyword = request.getKeyword();
//            String nvmid = request.getNvmid();
//
//            // 2. 토큰으로 userIdx 추출
//            int userIdx = 102;
//            request.setUserIdx(userIdx);
//            // 3. 순위 검색
//            int rank = rankSearchService.searchRank(keyword, nvmid);
//            //+ trackingDTO에 storeName이랑 productName도 추가해주자
//
//            // 4. 저장 처리
//            RankTrackingDTO trackingDTO = rankTrackingService.addOrUpdateRankTracking(request);
//
//            RankTrackingDateDTO dateDTO = new RankTrackingDateDTO();
//            dateDTO.setRankTrackingIdx(trackingDTO.getRankTrackingIdx().intValue());
//            dateDTO.setKeyword(keyword);
//            dateDTO.setNvmid(nvmid);
//            dateDTO.setRank(String.valueOf(rank));
//            dateDTO.setDate(LocalDate.now());
//
//            rankTrackingDateService.saveDailyRank(dateDTO);
//
//            return ResponseEntity.ok(Map.of("message", "순위 추적이 성공적으로 등록되었습니다."));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
//        }
//    }

//     실제로 쓸 코드
    @GetMapping("/list")
    public ResponseEntity<List<RankTrackingDTO>> getRankList(@RequestHeader("Authorization") String token) {

        // 1. 토큰으로 userIdx 추출
        Claims claims = jwtTokenProvider.parseClaims(token.replace("Bearer ", "")); // "Bearer " 제거
        Integer userIdx = claims.get("userIdx", Integer.class);
        if (userIdx == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 2. RankTrackingEntity에서 userIdx로 검색
        List<RankTrackingDTO> rankList = rankTrackingService.getRankListByUserIdx(userIdx);

        // 3. RankTrackingDTO를 JSON 형태로 반환
        return ResponseEntity.ok(rankList);
    }


    //     테스트로 쓸 코드
//    @GetMapping("/list")
//    public ResponseEntity<List<RankTrackingDTO>> getRankList() {
//
//
//        // 1. 토큰으로 userIdx 추출
//        Integer userIdx = 102;
//
//        // 2. RankTrackingEntity에서 userIdx로 검색
//        List<RankTrackingDTO> rankList = rankTrackingService.getRankListByUserIdx(userIdx);
//
//        // 3. RankTrackingDTO를 JSON 형태로 반환
//        return ResponseEntity.ok(rankList);
//    }


    //     테스트로 쓸 코드
//    @GetMapping("/read/{id}")
//    public ResponseEntity<List<RankTrackingDateDTO>> getRankDateList(@PathVariable("id") Long id) {
//        // 1. 받아온 id 추출
//        Long rankTrackingIdx = id;
//
//        // 2. RankTrackingDateEntity에서 RankTrackingIdx로 리스트 불러오기
//        List<RankTrackingDateDTO> rankTrackingDateDTOList = rankTrackingDateService.getAllRankTrackingDates(rankTrackingIdx);
//
//        // 3. rankTrackingDateDTOList를 JSON 형태로 반환
//        return ResponseEntity.ok(rankTrackingDateDTOList);
//    }
    @GetMapping("/read/{id}")
    public ResponseEntity<List<RankTrackingDateDTO>> getRankDateList(@PathVariable("id") Long id, @RequestHeader("Authorization") String token) {
        // 1. 토큰 검증
        Claims claims = jwtTokenProvider.parseClaims(token.replace("Bearer ", ""));
        if (claims == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 2. 받아온 id 추출
        Long rankTrackingIdx = id;

        // 3. RankTrackingDateEntity에서 RankTrackingIdx로 리스트 불러오기
        List<RankTrackingDateDTO> rankTrackingDateDTOList = rankTrackingDateService.getAllRankTrackingDates(rankTrackingIdx);

        // 4. rankTrackingDateDTOList를 JSON 형태로 반환
        return ResponseEntity.ok(rankTrackingDateDTOList);
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deleteTracking(@RequestBody RankTrackingDTO request, @RequestHeader("Authorization") String token) {
        try {
            System.out.println(request);
            // 요청에서 데이터 추출
            String keyword = request.getKeyword();
            System.out.println(keyword);
            String nvmid = request.getNvmid();
            System.out.println(nvmid);
            Long trackingIdx = request.getRankTrackingIdx();
            System.out.println(trackingIdx);

            // 1. 토큰으로 userIdx 추출
            Claims claims = jwtTokenProvider.parseClaims(token.replace("Bearer ", ""));
            if (claims == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Integer userIdx = claims.get("userIdx", Integer.class);
            System.out.println("userIdx: " + userIdx);
            // 2. 삭제 처리
            //rankTracking에서 해당하는 항목 삭제
            rankTrackingService.deleteTracking(userIdx, keyword, nvmid);

            //날짜별로 추가로 저장된 항목리스트 찾아서 또 삭제
            rankTrackingDateService.deleteAllByRankTrackingIdx(trackingIdx);
            System.out.println("삭제도 완료");
            return ResponseEntity.ok(Map.of("message", "삭제가 성공적으로 완료되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/traceslot")
    public ResponseEntity<?> getSlotCounts(@RequestHeader("Authorization") String token) {
        try {
            // 1. 토큰에서 Claims 파싱
            Claims claims = jwtTokenProvider.parseClaims(token.replace("Bearer ", ""));
            if (claims == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // 2. userIdx와 grade 추출
            Integer userIdx = claims.get("userIdx", Integer.class);
            String grade = claims.get("grade", String.class);

            // 3. 서비스 호출
            Map<String, Integer> slotCounts = rankTrackingService.getSlotCounts(userIdx, grade);

            // 4. 성공 응답 반환
            return ResponseEntity.ok(slotCounts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
}
