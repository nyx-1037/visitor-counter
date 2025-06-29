package com.nyx.visitorcounter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.nyx.visitorcounter.repository")
@EnableScheduling
public class VisitorCounterApplication {

    public static void main(String[] args) {
        SpringApplication.run(VisitorCounterApplication.class, args);
        System.out.println("访问量计数器系统启动成功！");
    }

}