package com.example.storeartbackend.Controller;

import com.example.storeartbackend.DTO.CallcenterDTO;
import com.example.storeartbackend.Entity.CallcenterEntity;
import com.example.storeartbackend.Service.CallcenterService;
import com.example.storeartbackend.Util.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/callcenter")
public class CallcetnerController {
    private final CallcenterService callcenterService;
    private final JwtTokenProvider jwtTokenProvider;
    // 등록
    @PostMapping
    public ResponseEntity<?> createCall(
            @RequestHeader(value = "Authorization", required = true) String token,
            @RequestBody CallcenterDTO callcenterDTO) {
        // Authorization 헤더 확인
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("유효하지 않은 Authorization 헤더입니다.");
        }

        // "Bearer "를 제외한 JWT 토큰만 추출
        String jwtToken = token.substring(7);

        try {
            // JWT 토큰 검증
            if (!jwtTokenProvider.validateToken(jwtToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
            }

            // 유효한 토큰에서 클레임 추출
            Claims claims = jwtTokenProvider.parseClaims(jwtToken);
            Integer userIdx = claims.get("userIdx", Integer.class); // userIdx 추출

            if (userIdx == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 사용자 정보입니다.");
            }

            // userIdx를 CallcenterDTO에 설정
            callcenterDTO.setUserIdx(userIdx);

            // 서비스 호출로 등록 처리
            CallcenterDTO createdCall = callcenterService.register(callcenterDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCall);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }


    @GetMapping
    public ResponseEntity<?> getAllCall(@RequestHeader(value = "Authorization", required = true) String token) {
        // Authorization 헤더가 없거나 잘못된 경우 처리
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("유효하지 않은 Authorization 헤더입니다.");
        }

        // "Bearer "를 제외한 JWT 토큰만 추출
        String jwtToken = token.substring(7);

        try {
            // JWT 토큰 검증
            if (!jwtTokenProvider.validateToken(jwtToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
            }

            // 유효한 토큰에서 클레임 추출
            Claims claims = jwtTokenProvider.parseClaims(jwtToken);
            Integer userIdx = claims.get("userIdx", Integer.class); // userIdx 추출
            String grade = claims.get("grade", String.class); // 사용자 등급 추출

            if (userIdx == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 사용자 정보입니다.");
            }

            if (!"6".equals(grade)) { // 등급 비교는 equals() 사용
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자만 접근 가능합니다.");
            }

            // 전체 글 리스트 반환
            List<CallcenterDTO> callList = callcenterService.list();
            return ResponseEntity.ok(callList);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    @GetMapping("/mycall")
    public ResponseEntity<?> getMycall(@RequestHeader(value = "Authorization", required = true) String token) {
        // Authorization 헤더가 없거나 잘못된 경우 처리
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("유효하지 않은 Authorization 헤더입니다.");
        }

        // "Bearer "를 제외한 JWT 토큰만 추출
        String jwtToken = token.substring(7);

        try {
            // JWT 토큰 검증
            if (!jwtTokenProvider.validateToken(jwtToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
            }

            // 유효한 토큰에서 클레임 추출
            Claims claims = jwtTokenProvider.parseClaims(jwtToken);
            Integer userIdx = claims.get("userIdx", Integer.class); // userIdx 추출

            if (userIdx == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 사용자 정보입니다.");
            }

            // userIdx를 사용하여 내 글 리스트 반환
            List<CallcenterDTO> myCallList = callcenterService.list1(userIdx);
            return ResponseEntity.ok(myCallList);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }


    // 개별 조회
    @GetMapping("/{id}")
    public ResponseEntity<CallcenterDTO> getCallById(@PathVariable("id") Long id) {
        return new ResponseEntity<>(callcenterService.read(id), HttpStatus.OK);
    }

    // 수정
    @PutMapping("/{id}")
    public ResponseEntity<CallcenterDTO> updateCall(@RequestBody CallcenterDTO callcenterDTO, @PathVariable Long id) {
        callcenterDTO.setCallcenterIdx(id);
        return new ResponseEntity<>(callcenterService.update(callcenterDTO), HttpStatus.OK);
    }

    // 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCall(@PathVariable Long id) {
        callcenterService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
