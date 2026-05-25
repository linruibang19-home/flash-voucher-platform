package org.javaup.shop;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("org.javaup.shop.mapper")
@SpringBootApplication
public class HmdpShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(HmdpShopApplication.class, args);
    }
}
