package com.example.storeartbackend.Util;

import com.example.storeartbackend.DTO.UserDTO;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final Key key;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = java.util.Base64.getDecoder().decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // JWT 토큰 생성 (AccessToken)
    // JWT 토큰 생성 (비밀번호 제외한 사용자 정보 포함)
    public String generateToken(UserDTO userDTO) {
        long now = (new Date()).getTime();
        Date validity = new Date(now + 86400000); // 토큰 만료 시간 (1일)

        return Jwts.builder()
                .setSubject(userDTO.getUserId())  // 사용자 ID를 Subject로 설정
                .claim("userIdx", userDTO.getUserIdx())  // 사용자 ID 추가
                .claim("userId", userDTO.getUserId())  // 사용자 ID 추가
                .claim("userName", userDTO.getUserName())  // 사용자 이름 추가
                .claim("userEmail", userDTO.getUserEmail())  // 사용자 이메일 추가
                .claim("userPhone", userDTO.getUserPhone())  // 사용자 전화번호 추가
                .claim("organization", userDTO.getOrganization())  // 소속 회사 추가
                .claim("useYn", userDTO.getUseYn())  // 사용 여부 추가
                .claim("mktEmail", userDTO.getMktEmail())  // 마케팅 이메일 수신 여부 추가
                .claim("mktSms", userDTO.getMktSms())  // 마케팅 SMS 수신 여부 추가
                .claim("mktAdr", userDTO.getMktAdr())  // 마케팅 주소 수신 여부 추가
                .claim("grade", userDTO.getGrade())  // 등급 추가
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();
    }


    // JWT 토큰에서 사용자 정보(Claims) 추출
    public Claims parseClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();
    }

    // JWT 토큰 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SecurityException | IllegalArgumentException e) {
            return false;
        }
    }
    // grade 추출 메서드
    public String extractGrade(String token) {
        Claims claims = parseClaims(token); // JWT에서 Claims 추출
        return claims.get("grade", String.class); // "grade" 클레임 값 반환
    }
}
