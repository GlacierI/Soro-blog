package com.glacier.soroblog.picture;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import springfox.documentation.oas.annotations.EnableOpenApi;


@EnableTransactionManagement
@SpringBootApplication
@EnableOpenApi
@EnableDiscoveryClient
@EnableFeignClients("com.glacier.soroblog.commons.feign")
@ComponentScan(basePackages = {
        "com.glacier.soroblog.commons.config.feign",
        "com.glacier.soroblog.commons.handler",
        "com.glacier.soroblog.commons.config.redis",
        "com.glacier.soroblog.utils",
        "com.glacier.soroblog.picture"})
public class PictureApplication {

    public static void main(String[] args) {
        SpringApplication.run(PictureApplication.class, args);
    }
}
