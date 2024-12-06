package com.example.storeartbackend.Controller;

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

import java.util.Map;

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
    @PostMapping("/register")
    public ResponseEntity<?> registerTracking(@RequestBody Map<String, String> request, @RequestHeader("Authorization") String token) {

        try {
            String keyword = request.get("keyword");
            String nvmid = request.get("nvmid");

            // 1. 토큰으로 userIdx 추출
            Claims claims = jwtTokenProvider.parseClaims(token);
            Integer userIdx = claims.get("userIdx", Integer.class);



            // 2. 순위 검색
            int rank = rankSearchService.searchRank(keyword, nvmid);
            // 3. 저장
            //랭킹트래킹 리스트에 하나 추가
            //
            //랭킹트래킹(날짜별)리스트에 오늘 날짜로 추가
            return ResponseEntity.ok(Map.of("message", "순위 추적이 성공적으로 등록되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

}
