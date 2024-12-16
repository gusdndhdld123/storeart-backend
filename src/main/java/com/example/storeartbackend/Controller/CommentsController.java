package com.example.storeartbackend.Controller;

import com.example.storeartbackend.DTO.CallcenterDTO;
import com.example.storeartbackend.DTO.CommentsDTO;
import com.example.storeartbackend.DTO.UserDTO;
import com.example.storeartbackend.Entity.CallcenterEntity;
import com.example.storeartbackend.Entity.CommentsEntity;
import com.example.storeartbackend.Service.CallcenterService;
import com.example.storeartbackend.Service.CommentsService;
import com.example.storeartbackend.Service.UserService;
import com.example.storeartbackend.Util.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/comments")
public class CommentsController {

    private final CommentsService commentsService;
    private final CallcenterService callcenterService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    // 등록
    @PostMapping
    public ResponseEntity<?> createComment(
            @RequestHeader(value = "Authorization", required = true) String token, // Authorization 헤더에서 토큰 추출
            @RequestBody CommentsDTO commentsDTO) {

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

            // userIdx를 통해 UserDTO 조회
            UserDTO userDTO = userService.read(userIdx); // userService는 userIdx로 사용자 정보를 조회

            // userEntity의 grade가 "6"일 경우 "관리자", 그렇지 않으면 "작성자"로 설정
            String writer = "6".equals(userDTO.getGrade()) ? "관리자" : "작성자";
            commentsDTO.setWriter(writer); // CommentsDTO의 writer 필드 설정

            // 댓글 등록 처리
            Long callcenterIdx = commentsDTO.getCallcenterIdx();
            CallcenterDTO callcenterDTO = callcenterService.read(callcenterIdx);

            // 관리자일 경우 callcenterDTO의 complete 값을 "Y"로 설정
            if ("6".equals(userDTO.getGrade())) {
                callcenterDTO.setComplete("Y");
            }

            // 업데이트 수행
            callcenterService.update(callcenterDTO);

            // DTO -> Entity 변환 후 서비스로 전달
            CommentsEntity createdComment = commentsService.addComment(commentsDTO);

            return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }



    // 전체 조회
    @GetMapping
    public ResponseEntity<List<CommentsDTO>> getAllComments(@RequestParam Long id) {
        Long callcenterIdx = id;
        System.out.println(callcenterIdx);

        // callcenterIdx를 파라미터로 받아 해당 댓글들을 조회
        List<CommentsDTO> comments = commentsService.getAllComments(callcenterIdx);
        System.out.println(comments);
        return new ResponseEntity<>(comments, HttpStatus.OK);
    }


    // 수정
    @PutMapping("/{id}")
    public ResponseEntity<CommentsEntity> updateComment(@RequestBody CommentsDTO commentsDTO, @PathVariable Long id) {
        // 댓글 수정: ID 설정 후 서비스로 전달
        commentsDTO.setCommentIdx(id);
        CommentsEntity updatedComment = commentsService.updateComment(commentsDTO);
        return new ResponseEntity<>(updatedComment, HttpStatus.OK);
    }

    // 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        // 댓글 삭제 서비스 호출
        commentsService.deleteComment(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
