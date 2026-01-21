package com.tamarcado;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
public class TaMarcadoApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaMarcadoApplication.class, args);
    }
}