package com.nyx.visitorcounter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 访问量计数器应用程序入口类
 * 
 * @SpringBootApplication 标识这是一个Spring Boot应用程序
 * @MapperScan 自动扫描指定包下的MyBatis Mapper接口
 * @EnableScheduling 启用定时任务调度功能，用于Redis数据定期同步到MySQL
 */
@SpringBootApplication
@MapperScan("com.nyx.visitorcounter.repository")
@EnableScheduling
public class VisitorCounterApplication {

    /**
     * 应用程序主入口方法
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(VisitorCounterApplication.class, args);
        System.out.println("访问量计数器系统启动成功！");
    }

}