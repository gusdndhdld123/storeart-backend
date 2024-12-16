package com.example.storeartbackend.Controller;

import com.example.storeartbackend.DTO.CallcenterDTO;
import com.example.storeartbackend.DTO.NoticeDTO;
import com.example.storeartbackend.Entity.NoticeEntity;
import com.example.storeartbackend.Repository.NoticeRepository;
import com.example.storeartbackend.Service.NoticeService;
import com.example.storeartbackend.Util.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/notice")
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class NoticeController {
    private final NoticeService noticeService;
    private final ModelMapper modelMapper;
    private final JwtTokenProvider jwtTokenProvider;


    @PostMapping
    public ResponseEntity<?> createNotice(@RequestBody NoticeDTO noticeDTO, @RequestHeader(value = "Authorization", required = true) String token) {
        // Authorization 헤더 확인
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("유효하지 않은 Authorization 헤더입니다.");
        }

        // "Bearer "를 제외한 JWT 토큰만 추출
        String jwtToken = token.substring(7);

        try {
            // JWT 토큰 검증
            if (!jwtTokenProvider.validateToken(jwtToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("유효하지 않은 토큰입니다.");
            }

            // 유효한 토큰에서 클레임 추출
            Claims claims = jwtTokenProvider.parseClaims(jwtToken);
            String grade = claims.get("grade", String.class); // userIdx 추출

            // 관리자인지 확인
            if (!"6".equals(grade)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("관리자만 공지사항 작성이 가능합니다.");
            }

            // 공지사항 등록
            NoticeDTO createdNotice = noticeService.register(noticeDTO);
            return new ResponseEntity<>(createdNotice, HttpStatus.CREATED);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("서버 오류가 발생했습니다.");
        }
    }



    // 전체 조회
    @GetMapping
    public ResponseEntity<List<NoticeDTO>> getAllNotices() {
        return new ResponseEntity<>(noticeService.list(), HttpStatus.OK);
    }

    // 개별 조회
    @GetMapping("/{id}")
    public ResponseEntity<NoticeDTO> getNoticeById(@PathVariable("id") Long id) {
        return new ResponseEntity<>(noticeService.read(id), HttpStatus.OK);
    }

    // 수정
    @PutMapping("/{id}")
    public ResponseEntity<NoticeDTO> updateNotice(@RequestBody NoticeDTO noticeDTO, @PathVariable Long id) {
        noticeDTO.setNoticeIdx(id);
        return new ResponseEntity<>(noticeService.update(noticeDTO), HttpStatus.OK);
    }

    // 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotice(@PathVariable Long id, @RequestHeader(value = "Authorization", required = true) String token) {
        // Authorization 헤더 확인
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("유효하지 않은 Authorization 헤더입니다.");
        }

        // "Bearer "를 제외한 JWT 토큰만 추출
        String jwtToken = token.substring(7);

        try {
            // JWT 토큰 검증
            if (!jwtTokenProvider.validateToken(jwtToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("유효하지 않은 토큰입니다.");
            }

            // 유효한 토큰에서 클레임 추출
            Claims claims = jwtTokenProvider.parseClaims(jwtToken);
            String grade = claims.get("grade", String.class); // userIdx 추출

            // 관리자인지 확인
            if (!"6".equals(grade)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("관리자만 공지사항 삭제가 가능합니다.");
            }

            // 공지사항 삭제
            noticeService.delete(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("서버 오류가 발생했습니다.");
        }
    }


}
