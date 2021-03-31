package com.supconit.zzzhly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.supconit")
@ComponentScan(basePackages = "com.supconit.**")
@RefreshScope
@EnableScheduling
public class ZzzhlyApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZzzhlyApplication.class, args);
    }

}
