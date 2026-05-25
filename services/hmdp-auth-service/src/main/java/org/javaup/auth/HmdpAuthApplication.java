package org.javaup.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("org.javaup.auth.mapper")
@SpringBootApplication
public class HmdpAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(HmdpAuthApplication.class, args);
    }
}
