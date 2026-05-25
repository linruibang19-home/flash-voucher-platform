package org.javaup.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy(exposeProxy = true)
@EnableFeignClients(basePackages = {"org.javaup.api.voucher", "org.javaup.api.notify"})
@MapperScan("org.javaup.order.mapper")
@SpringBootApplication
public class HmdpOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(HmdpOrderApplication.class, args);
    }
}
