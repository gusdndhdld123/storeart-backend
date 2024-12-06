package com.example.storeartbackend.Controller;

import com.example.storeartbackend.Entity.NaverSearchCountEntity;
import com.example.storeartbackend.Entity.SearchCountEntity;
import com.example.storeartbackend.Repository.NaverSearchCountRepository;
import com.example.storeartbackend.Repository.SearchcountRepository;
import com.example.storeartbackend.Service.NaverSearchCountService;
import com.example.storeartbackend.Service.SearchcountService;
import com.example.storeartbackend.Util.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.time.LocalDate;
import java.util.Map;

@Controller
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class NaverSearchCountController {
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private final NaverSearchCountService naverSearchCountService;
    @Autowired
    private final NaverSearchCountRepository naverSearchCountRepository;


    @GetMapping("/naver/searchcount")
    public ResponseEntity<Map<String, String>> searchcount(@RequestHeader(value = "Authorization", required = true) String token) {


        if (token == null || !token.startsWith("Bearer ")) {
            System.out.println("Error: Missing or invalid Authorization header.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Missing or invalid Authorization header."));
        }

        // "Bearer "를 제외한 JWT 토큰만 추출
        String jwtToken = token.substring(7);

        try {
            // JWT 토큰 검증 (JwtTokenProvider 사용)
            if (!jwtTokenProvider.validateToken(jwtToken)) {
                System.out.println("Error: Invalid JWT token.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid token."));
            }

            // 토큰이 유효하면, 유저 정보를 추출
            Claims claims = jwtTokenProvider.parseClaims(jwtToken);
            Integer userIdx = claims.get("userIdx", Integer.class);  // userIdx를 Integer로 추출



            // 유저 정보와 날짜를 사용하여 searchcountService 호출
            NaverSearchCountEntity searchCount = naverSearchCountService.getSearchCount(userIdx, LocalDate.now());

            // searchcount와 maxcount 값을 가져오기
            String searchcount = String.valueOf(searchCount.getSearchCount());
            String maxcount = String.valueOf(searchCount.getMaxSearch());

            // 정상적으로 처리된 경우 searchcount와 maxcount를 포함한 JSON 응답 반환
            Map<String, String> response = Map.of(
                    "searchcount", searchcount,
                    "maxcount", maxcount
            );

            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (Exception e) {
            e.printStackTrace();
            // 예외 발생 시 에러 메시지를 포함한 JSON 반환
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error: " + e.getMessage()));
        }
    }

    @PostMapping("/naver/increment")
    public ResponseEntity<?> incrementSearchCount(@RequestHeader(value = "Authorization") String token) {
        try {
            // JWT 토큰에서 userIdx 추출
            String jwtToken = token.substring(7); // "Bearer "를 제외한 토큰만 추출
            Claims claims = jwtTokenProvider.parseClaims(jwtToken);
            Integer userIdx = claims.get("userIdx", Integer.class);  // userIdx 추출

            if (userIdx == null) {
                return ResponseEntity.badRequest().body("Error: userIdx is missing in token.");
            }

            // searchCount 증가 처리
            naverSearchCountService.plusSearchCount(userIdx); // searchCount +1 증가

            return ResponseEntity.ok("Search count updated successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

}
