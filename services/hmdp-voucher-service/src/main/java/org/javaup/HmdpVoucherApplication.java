package org.javaup;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("org.javaup.mapper")
@SpringBootApplication
public class HmdpVoucherApplication {

    public static void main(String[] args) {
        SpringApplication.run(HmdpVoucherApplication.class, args);
    }

}
