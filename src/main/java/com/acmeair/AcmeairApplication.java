package com.acmeair;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class AcmeairApplication {

    public static void main(String[] args) {
        SpringApplication.run(AcmeairApplication.class, args);
    }

}
