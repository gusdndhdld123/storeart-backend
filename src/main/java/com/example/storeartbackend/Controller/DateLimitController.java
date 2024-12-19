package com.example.storeartbackend.Controller;

import com.example.storeartbackend.Service.DateLimitService;
import com.example.storeartbackend.Service.PaymentService;
import com.example.storeartbackend.Util.JwtTokenProvider;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/datelimit")
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class DateLimitController {
    private final PaymentService paymentService;
    private final JwtTokenProvider jwtUtil;
    private final DateLimitService dateLimitService;

    @GetMapping("/{userIdx}")
    public ResponseEntity<Integer> getRemainingDays(@PathVariable int userIdx) {
        try {
            // 유저의 남은 구독 일수 계산
            int remainingDays = dateLimitService.calculateRemainingDays(userIdx);
            System.out.println("남은 구독 일수 :" + remainingDays);
            // 남은 구독 일수를 반환
            return ResponseEntity.ok(remainingDays);

        } catch (Exception e) {
            // 예외 처리: HTTP 500 상태 코드 응답
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
