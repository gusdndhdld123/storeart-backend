package com.example.storeartbackend.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;



@Service
public class RankSearchService {
    public int searchRank(String keyword, String nvmid) {
        int rank = 0;

        RestTemplate restTemplate = new RestTemplate();

        for (int start = 1; start <= 400; start += 100) {
            String apiUrl = "https://openapi.naver.com/v1/search/shop.json?query=" + keyword + "&start=" + start + "&display=100";

            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Naver-Client-Id", "7rmKKJiRBx4F3LGQNYUW");
            headers.add("X-Naver-Client-Secret", "6CieeCU14C");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            try {
                ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class);

                if (response.getStatusCode() == HttpStatus.OK) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode root = objectMapper.readTree(response.getBody());
                    JsonNode items = root.path("items");

                    // 검색 결과가 없는 경우
                    if (items.size() == 0) {
                        break;
                    }

                    // 검색 결과 중 productId가 일치하는 항목 찾기
                    for (int j = 0; j < items.size(); j++) {
                        JsonNode item = items.get(j);
                        if (item.path("productId").asText().equals(nvmid)) {
                            rank = start + j; // 순위 계산
                            return rank;
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error during API call or JSON parsing: " + e.getMessage());
            }
        }

        // 400위 밖이거나 결과가 없는 경우 0 반환
        return rank;
    }
}
