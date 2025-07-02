package com.nyx.visitorcounter.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * IP地理位置服务测试类
 */
@SpringBootTest
@ActiveProfiles("test")
public class IpLocationServiceTest {

    @Autowired
    private IpLocationService ipLocationService;

    @Test
    public void testPublicIpLocation() {
        // 测试公网IP地理位置查询
        String publicIp = "117.163.235.78"; // 广州移动IP
        String location = ipLocationService.getLocationByIp(publicIp);
        System.out.println("公网IP " + publicIp + " 的地理位置: " + location);
    }

    @Test
    public void testLocalIpLocation() {
        // 测试本地IP
        String localIp = "127.0.0.1";
        String location = ipLocationService.getLocationByIp(localIp);
        System.out.println("本地IP " + localIp + " 的地理位置: " + location);
    }

    @Test
    public void testInvalidIpLocation() {
        // 测试无效IP
        String invalidIp = "invalid.ip";
        String location = ipLocationService.getLocationByIp(invalidIp);
        System.out.println("无效IP " + invalidIp + " 的地理位置: " + location);
    }

    @Test
    public void testNullIpLocation() {
        // 测试空IP
        String location = ipLocationService.getLocationByIp(null);
        System.out.println("空IP的地理位置: " + location);
    }

    @Test
    public void testMultiplePublicIps() {
        // 测试多个公网IP
        String[] publicIps = {
            "8.8.8.8",        // Google DNS
            "114.114.114.114", // 114 DNS
            "117.163.235.78",  // 广州移动
            "220.181.38.148"   // 百度
        };
        
        for (String ip : publicIps) {
            String location = ipLocationService.getLocationByIp(ip);
            System.out.println("IP " + ip + " 的地理位置: " + location);
        }
    }
}