package org.javaup.registry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @description: Eureka 服务注册中心
 * @maintainer: lrb
 **/
@EnableEurekaServer
@SpringBootApplication
public class HmdpRegistryApplication {

    public static void main(String[] args) {
        SpringApplication.run(HmdpRegistryApplication.class, args);
    }
}
