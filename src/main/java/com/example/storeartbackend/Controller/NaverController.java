package com.example.storeartbackend.Controller;

import com.example.storeartbackend.Entity.SearchCountEntity;
import com.example.storeartbackend.Repository.SearchcountRepository;
import com.example.storeartbackend.Service.SearchcountService;
import com.example.storeartbackend.Util.JwtTokenProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@RestController
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class NaverController {

    @Autowired
    private WebDriver webDriver;
    @Autowired
    private ApplicationContext context; // Spring Context를 사용하여 애플리케이션 재시작

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private final SearchcountService searchcountService;
    @Autowired
    private final SearchcountRepository searchcountRepository;

    private final String clientId = "7rmKKJiRBx4F3LGQNYUW";

    private final String clientSecret = "6CieeCU14C";

    @GetMapping("/api/searchcount")
    public ResponseEntity<Map<String, String>> searchcount(@RequestHeader(value = "Authorization", required = true) String token) {
        System.out.println("get searchcount");
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

            // userIdx는 JWT의 userIdx 값이나 다른 식별자를 사용
            // userIdx는 Integer 타입으로 사용 가능
            System.out.println("searchcount api userIdx: " + userIdx);

            // 유저 정보와 날짜를 사용하여 searchcountService 호출
            SearchCountEntity searchCount = searchcountService.getSearchCount(userIdx, LocalDate.now());

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

    @PostMapping("/increment")
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
            searchcountService.plusSearchCount(userIdx); // searchCount +1 증가

            return ResponseEntity.ok("Search count updated successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }




    @GetMapping("/api/{keyword}")
    @ResponseBody
    public String api(@PathVariable String keyword) {
        Future<String> future = executorService.submit(() -> getCombinedSearchData(keyword));

        try {
            return future.get();
        } catch (Exception e) {
            e.printStackTrace();
            // 예외 발생 시 서버 재시작

            return "Error: " + e.getMessage();
        }
    }


    private String getCombinedSearchData(String keyword) {
        try {
            // ChromeDriver 초기화 설정
            ChromeOptions options = new ChromeOptions();
            options.addArguments( "--no-sandbox", "--disable-dev-shm-usage", "--window-size=1920x1080");
            if (webDriver == null) {
                webDriver = new ChromeDriver(options);
            }

            // 첫 번째 요청 처리
            String firstJsonData = null;
            List<JsonNode> combinedDataArray = new ArrayList<>();
            ObjectMapper mapper = new ObjectMapper();

            int[] cursors = {1, 101, 201, 301, 401}; // 여러 요청의 커서 값
            for (int i = 0; i < cursors.length; i++) {
                int cursor = cursors[i];

                // 각 페이지 요청 (단순히 JSON 데이터를 받아옴)
                webDriver.get("https://search.shopping.naver.com/ns/v1/search/paged-composite-cards?cursor="
                        + cursor
                        + "&pageSize=100&query=" + keyword
                        + "&searchMethod=all.basic&isFreshCategory=false&isOriginalQuerySearch=false&listPage=1&hiddenNonProductCard=false&hasMoreAd=false");

                // JSON 데이터 추출 (페이지 소스에서 JSON 부분만 추출)
                String pageSource = webDriver.getPageSource();
                int startIndex = pageSource.indexOf("{");
                int endIndex = pageSource.lastIndexOf("}");
                if (startIndex < 0 || endIndex < 0) {
                    return "Error: 데이터가 존재하지 않습니다.";
                }

                String jsonData = pageSource.substring(startIndex, endIndex + 1);

                // JSON 파싱
                JsonNode rootNode = mapper.readTree(jsonData);

                // 첫 번째 요청 데이터 유지
                if (i == 0) {
                    firstJsonData = jsonData;
                    JsonNode firstDataArray = rootNode.path("data").path("data"); // "data.data" 배열
                    if (firstDataArray.isArray()) {
                        combinedDataArray.addAll(mapper.convertValue(firstDataArray, List.class));
                    }
                } else {
                    // 이후 요청의 "data.data" 배열만 추가
                    JsonNode additionalDataArray = rootNode.path("data").path("data");
                    if (additionalDataArray.isArray()) {
                        combinedDataArray.addAll(mapper.convertValue(additionalDataArray, List.class));
                    }
                }
            }

            // 합쳐진 데이터를 첫 번째 JSON에 병합
            JsonNode firstJsonNode = mapper.readTree(firstJsonData);
            ((ObjectNode) firstJsonNode.path("data")).set("data", mapper.valueToTree(combinedDataArray));

            // 결과 JSON 문자열 반환
            String result = mapper.writeValueAsString(firstJsonNode);

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            // WebDriver가 오류를 일으켰다면 새로 생성
            if (webDriver != null) {
                webDriver.quit();  // 기존 WebDriver 종료
            }

            return "Error: " + e.getMessage();
        }
    }
    @PostMapping("/nowsearch")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getNowSearch(@RequestBody Map<String, String> request) {

        int rank = 0;
        JsonNode matchedItem = null;  // JsonNode 타입으로 설정
        JsonNode total = null;  // total 초기화
        String keyword = request.get("keyword");
        String nvmid = request.get("nvmid");


        RestTemplate restTemplate = new RestTemplate();

        // 반복문으로 Naver API 요청을 10번씩 시도
        for (int start = 1; start <= 400; start += 100) {
            String apiUrl = "https://openapi.naver.com/v1/search/shop.json?query=" + keyword + "&start=" + start + "&display=100";

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Naver-Client-Id", "7rmKKJiRBx4F3LGQNYUW");
            headers.set("X-Naver-Client-Secret", "6CieeCU14C");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            try {
                ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class);

                if (response.getStatusCode() == HttpStatus.OK) {
                    String body = response.getBody();
                    // JSON 데이터 파싱
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode root = objectMapper.readTree(body);
                    JsonNode items = root.path("items");
                    total = root.path("total");  // total 저장

                    // 검색 결과가 없으면 바로 응답
                    if (items.size() == 0) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(Map.of("rank", 0, "matchedItem", null, "message", "해당 상품이 400위 밖에 있거나 검색 결과에서 찾을 수 없습니다."));
                    }

                    // 검색 결과 중 productId가 일치하는 항목 찾기
                    for (int j = 0; j < items.size(); j++) {
                        JsonNode item = items.get(j);
                        if (item.path("productId").asText().equals(nvmid)) {
                            rank = start + j;
                            matchedItem = item;  // JSON 객체로 저장
                            break;
                        }
                    }

                    if (rank > 0 && matchedItem != null) {
                        break;  // 순위와 아이템을 찾으면 루프 종료
                    }
                }
            } catch (Exception e) {
                System.err.println("Error during API call or JSON parsing: " + e.getMessage());
            }
        }

        // rank가 없으면 404로 처리
        if (rank == 0 || matchedItem == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("rank", rank, "matchedItem", matchedItem, "message", "해당 상품이 400위 밖에 있거나 검색 결과에서 찾을 수 없습니다."));
        }

        // 정상적으로 아이템 찾은 경우
        return ResponseEntity.ok(Map.of("rank", rank, "matchedItem", matchedItem, "total", total));
    }

    @PostMapping("/nowsearch2")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getNowSearch2(@RequestBody Map<String, String> request) {

        int rank = 0;
        JsonNode total = null;  // total 초기화
        String keyword = request.get("keyword");
        String stname = request.get("stname");  // "포함되는" stname 값 추가


        RestTemplate restTemplate = new RestTemplate();

        // matchedItem과 rank를 배열로 선언
        List<JsonNode> matchedItems = new ArrayList<>();
        List<Integer> ranks = new ArrayList<>();

        // 반복문으로 Naver API 요청을 10번씩 시도
        for (int start = 1; start <= 400; start += 100) {
            String apiUrl = "https://openapi.naver.com/v1/search/shop.json?query=" + keyword + "&start=" + start + "&display=100";

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Naver-Client-Id", "7rmKKJiRBx4F3LGQNYUW");
            headers.set("X-Naver-Client-Secret", "6CieeCU14C");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            try {
                ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class);

                if (response.getStatusCode() == HttpStatus.OK) {

                    String body = response.getBody();
                    // JSON 데이터 파싱
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode root = objectMapper.readTree(body);
                    JsonNode items = root.path("items");
                    total = root.path("total");  // total 저장

                    // 검색 결과가 없으면 바로 응답
                    if (items.size() == 0) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(Map.of("rank", 0, "matchedItem", null, "message", "해당 상품이 400위 밖에 있거나 검색 결과에서 찾을 수 없습니다."));
                    }

                    // 검색 결과 중 stname이 포함된 항목들을 찾기
                    for (int j = 0; j < items.size(); j++) {
                        JsonNode item = items.get(j);
                        // 상품명(stname)이 포함되는지 체크
                        if (item.path("mallName").asText().contains(stname)) {
                            matchedItems.add(item);  // matchedItems 배열에 항목 추가
                            rank = start + j;
                            ranks.add(rank);  // rank 배열에 순위 추가
                        }
                    }

                    // 만약 matchedItems에 하나 이상의 항목이 추가되었다면 종료
                    if (!matchedItems.isEmpty()) {
                        break;
                    }
                }
            } catch (Exception e) {
                System.err.println("Error during API call or JSON parsing: " + e.getMessage());
            }
        }

        // matchedItems가 비어있으면 404로 처리
        if (matchedItems.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("rank", ranks, "matchedItem", matchedItems, "message", "해당 상품이 400위 밖에 있거나 검색 결과에서 찾을 수 없습니다."));
        }

        // 정상적으로 아이템을 찾은 경우
        return ResponseEntity.ok(Map.of("rank", ranks, "matchedItem", matchedItems, "total", total));
    }








}
