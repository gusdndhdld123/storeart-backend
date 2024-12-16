package com.example.storeartbackend.Config;

import jakarta.annotation.PreDestroy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebDriverConfig {
    private WebDriver driver;

    @Bean
    public WebDriver webDriver() {
        if (driver == null) {
            ChromeOptions options = new ChromeOptions();
            options.addArguments( "--no-sandbox", "--window-size=1920x1080");
            options.addArguments("--disable-dev-shm-usage");
//            options.addArguments("--headless");
//            options.setBinary("/usr/bin/google-chrome-stable");  // 리눅스용 경로.

            options.setBinary("C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe");//윈도우용 경로
            driver = new ChromeDriver(options);
            driver.manage().window().maximize();
        }
        return driver;
    }

    @PreDestroy
    public void closeDriver() {
        if (driver != null) {
            driver.quit();
        }
    }
}
