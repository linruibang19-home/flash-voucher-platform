package org.javaup.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("org.javaup.user.mapper")
@SpringBootApplication
public class HmdpUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(HmdpUserApplication.class, args);
    }
}
