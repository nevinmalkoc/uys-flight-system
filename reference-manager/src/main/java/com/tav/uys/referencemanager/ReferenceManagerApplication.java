package com.tav.uys.referencemanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ReferenceManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReferenceManagerApplication.class, args);
    }
}