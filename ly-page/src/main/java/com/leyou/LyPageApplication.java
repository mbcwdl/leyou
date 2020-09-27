package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/23 22:16
 */
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class LyPageApplication {

    public static void main(String[] args) {
        SpringApplication.run(LyPageApplication.class);
    }
}
