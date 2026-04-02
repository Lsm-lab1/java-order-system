package com.example.ordersystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class OrderSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderSystemApplication.class, args);
        System.out.println("========================================");
        System.out.println("电商订单系统已启动!");
        System.out.println("访问地址: http://localhost:8080");
        System.out.println("========================================");
        System.out.println("数据库配置: MySQL (order_system_db)");
        System.out.println("========================================");
    }
}
