package com.xust.secondhand;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 西南科技大学校园二手交易平台启动类
 */
@SpringBootApplication
@MapperScan("com.xust.secondhand.mapper")
public class SecondhandApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecondhandApplication.class, args);
    }
}
