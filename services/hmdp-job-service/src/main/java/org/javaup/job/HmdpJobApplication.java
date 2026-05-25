package org.javaup.job;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAspectJAutoProxy(exposeProxy = true)
@EnableFeignClients(basePackages = {"org.javaup.api.voucher", "org.javaup.api.order"})
@EnableScheduling
@SpringBootApplication
public class HmdpJobApplication {

    public static void main(String[] args) {
        SpringApplication.run(HmdpJobApplication.class, args);
    }
}
