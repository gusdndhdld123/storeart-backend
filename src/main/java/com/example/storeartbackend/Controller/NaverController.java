package com.example.storeartbackend.Controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@CrossOrigin(origins = "*")
public class NaverController {

    @Autowired
    private WebDriver webDriver;

    @Autowired
    private ApplicationContext context; // Spring Context를 사용하여 애플리케이션 재시작

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

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
            options.addArguments("--headless", "--disable-gpu", "--no-sandbox", "--disable-dev-shm-usage", "--window-size=1920x1080");
            if (webDriver == null) {
                webDriver = new ChromeDriver(options);
            }

            // 첫 번째 요청 처리
            String firstJsonData = null;
            List<JsonNode> combinedDataArray = new ArrayList<>();
            ObjectMapper mapper = new ObjectMapper();

            int[] cursors = {1, 101, 201, 301}; // 여러 요청의 커서 값
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




}
