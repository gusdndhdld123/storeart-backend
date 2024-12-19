package com.example.storeartbackend.Controller;

import com.example.storeartbackend.DTO.DateLimitDTO;
import com.example.storeartbackend.DTO.PaymentDTO;
import com.example.storeartbackend.Service.DateLimitService;
import com.example.storeartbackend.Service.PaymentService;
import com.example.storeartbackend.Util.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;
@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Controller
public class WidgetController {

    private final PaymentService paymentService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final JwtTokenProvider jwtUtil;
    private final DateLimitService dateLimitService;



    @RequestMapping(value = "/confirm")
    public ResponseEntity<JSONObject> confirmPayment(
            @RequestBody String jsonBody,
            @RequestHeader("Authorization") String authorization
    ) {
        // JWT 토큰에서 Bearer 제거 및 파싱
        String token = authorization.replace("Bearer ", "");
        Claims claims;
        try {
            claims = jwtUtil.parseClaims(token);
        } catch (Exception e) {
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("message", "Unauthorized access: Invalid token.");
            errorResponse.put("code", "UNAUTHORIZED");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        // Claims에서 userIdx 추출
        Integer userIdx1 = claims.get("userIdx", Integer.class);
        int userIdx = userIdx1;

        // JSON 요청 데이터 파싱
        JSONParser parser = new JSONParser();
        String orderId;
        String amount;
        String paymentKey;
        try {
            JSONObject requestData = (JSONObject) parser.parse(jsonBody);
            paymentKey = (String) requestData.get("paymentKey");
            orderId = (String) requestData.get("orderId");
            amount = (String) requestData.get("amount");
        } catch (ParseException e) {
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("message", "Invalid request: Unable to parse payment data.");
            errorResponse.put("code", "INVALID_REQUEST");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        // 토스페이먼츠 API 호출
        String widgetSecretKey = "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6";
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encodedBytes = encoder.encode((widgetSecretKey + ":").getBytes(StandardCharsets.UTF_8));
        String authorizations = "Basic " + new String(encodedBytes);

        try {
            // API 요청 Body 생성
            JSONObject obj = new JSONObject();
            obj.put("orderId", orderId);
            obj.put("amount", amount);
            obj.put("paymentKey", paymentKey);

            // 토스 API 호출
            URL url = new URL("https://api.tosspayments.com/v1/payments/confirm");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Authorization", authorizations);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            // 요청 전송
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(obj.toString().getBytes(StandardCharsets.UTF_8));

            // 응답 처리
            int code = connection.getResponseCode(); // 응답 코드
            boolean isSuccess = code == 200;

            InputStream responseStream = isSuccess ? connection.getInputStream() : connection.getErrorStream();
            Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8);
            JSONObject jsonObject = (JSONObject) parser.parse(reader);
            responseStream.close();

            // 응답 확인
            if (!isSuccess) {
                JSONObject errorResponse = new JSONObject();
                errorResponse.put("message", jsonObject.get("message"));
                errorResponse.put("code", jsonObject.get("code"));
                return ResponseEntity.status(code).body(errorResponse);
            }

            // JSON 응답 데이터를 PaymentDTO로 변환 및 저장
            PaymentDTO paymentDTO = new PaymentDTO();
            paymentDTO.setUserIdx(userIdx); // JWT에서 추출한 userIdx
            paymentDTO.setPaymentKey((String) jsonObject.get("paymentKey"));
            paymentDTO.setOrderId((String) jsonObject.get("orderId"));
            paymentDTO.setOrderName((String) jsonObject.get("orderName"));
            paymentDTO.setMethod((String) jsonObject.get("method"));
            paymentDTO.setTotalAmount((Long) jsonObject.get("totalAmount"));
            paymentDTO.setBalanceAmount((Long) jsonObject.get("balanceAmount"));
            paymentDTO.setCurrency((String) jsonObject.get("currency"));
            paymentDTO.setStatus((String) jsonObject.get("status"));
            paymentDTO.setApprovedAt((String) jsonObject.get("approvedAt"));

            // Receipt URL 처리
            JSONObject receiptObject = (JSONObject) jsonObject.get("receipt");
            if (receiptObject != null) {
                paymentDTO.setReceiptUrl((String) receiptObject.get("url"));
            }

            paymentService.savePayment(paymentDTO);

            // 기존 구독권 삭제 및 새로운 구독 기간 저장
            dateLimitService.deleteDateLimitsByUserIdx(userIdx);

            LocalDate startDate = LocalDate.now();
            LocalDate endDate = startDate.plusDays(30);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            DateLimitDTO dateLimitDTO = new DateLimitDTO();
            dateLimitDTO.setUserIdx(userIdx);
            dateLimitDTO.setStartDate(startDate.format(formatter));
            dateLimitDTO.setEndDate(endDate.format(formatter));
            System.out.println(dateLimitDTO.toString());
            dateLimitService.saveDateLimit(dateLimitDTO);

            return ResponseEntity.ok(jsonObject); // 성공적으로 처리 후 응답 반환

        } catch (Exception e) {
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("message", "Payment confirmation failed: " + e.getMessage());
            errorResponse.put("code", "CONFIRMATION_FAILED");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping
    @RequestMapping(value = "/orders")
    public ResponseEntity<Object> saveOrder(@RequestBody Map<String, Object> orderData) {
        String orderId = (String) orderData.get("orderId");
        Integer amount = (Integer) orderData.get("amount");

        // 검증 & 저장 로직 추가
        if (orderId == null || amount == null) {
            return ResponseEntity.badRequest().body("잘못된 요청 데이터입니다.");
        }

        // DB 저장 또는 메모리에 저장하는 로직 필요
        System.out.println("Order 저장 완료: orderId=" + orderId + ", amount=" + amount);

        return ResponseEntity.ok().body("Order 저장 성공");
    }
}