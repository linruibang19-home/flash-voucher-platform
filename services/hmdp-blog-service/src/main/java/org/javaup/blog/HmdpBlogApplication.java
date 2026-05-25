package org.javaup.blog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("org.javaup.blog.mapper")
@SpringBootApplication
public class HmdpBlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(HmdpBlogApplication.class, args);
    }
}
