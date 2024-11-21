package com.example.storeartbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class StoreartBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(StoreartBackendApplication.class, args);
    }

}
