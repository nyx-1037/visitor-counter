package com.nyx.visitorcounter.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

/**
 * IP地理位置查询服务
 * 使用ip-api.com免费API查询IP地址的地理位置信息
 */
@Service
public class IpLocationService {

    private static final Logger logger = LoggerFactory.getLogger(IpLocationService.class);
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    // ip-api.com API地址，支持中文显示
    private static final String IP_API_URL = "http://ip-api.com/json/{ip}?lang=zh-CN";
    
    @Autowired
    public IpLocationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 根据IP地址查询地理位置信息
     * 
     * @param ipAddress IP地址
     * @return 地理位置信息字符串，格式：国家-省份-城市-运营商
     */
    public String getLocationByIp(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            logger.warn("IP地址为空，无法查询地理位置");
            return "未知位置";
        }
        
        // 过滤本地IP地址
        if (isLocalIp(ipAddress)) {
            logger.debug("检测到本地IP地址: {}", ipAddress);
            return "本地网络";
        }
        
        try {
            logger.info("开始查询公网IP地址地理位置: {}", ipAddress);
            
            // 调用ip-api.com API
            String response = restTemplate.getForObject(IP_API_URL, String.class, ipAddress);
            
            logger.info("API响应内容，IP: {}, 响应: {}", ipAddress, response);
            
            if (response == null || response.trim().isEmpty()) {
                logger.warn("API返回空响应，IP: {}", ipAddress);
                return "查询失败";
            }
            
            // 解析JSON响应
            JsonNode jsonNode = objectMapper.readTree(response);
            
            // 检查API调用是否成功
            String status = jsonNode.path("status").asText();
            logger.info("API查询状态，IP: {}, 状态: {}", ipAddress, status);
            
            if (!"success".equals(status)) {
                String message = jsonNode.path("message").asText("未知错误");
                logger.warn("API查询失败，IP: {}, 状态: {}, 消息: {}", ipAddress, status, message);
                return "查询失败: " + message;
            }
            
            // 提取地理位置信息
            String country = jsonNode.path("country").asText("");
            String regionName = jsonNode.path("regionName").asText("");
            String city = jsonNode.path("city").asText("");
            String isp = jsonNode.path("isp").asText("");
            
            logger.info("解析地理位置信息，IP: {}, 国家: {}, 省份: {}, 城市: {}, 运营商: {}", 
                ipAddress, country, regionName, city, isp);
            
            // 构建地理位置字符串
            StringBuilder location = new StringBuilder();
            
            if (!country.isEmpty() && !"null".equals(country)) {
                location.append(country);
            }
            
            if (!regionName.isEmpty() && !"null".equals(regionName)) {
                if (location.length() > 0) location.append("-");
                location.append(regionName);
            }
            
            if (!city.isEmpty() && !"null".equals(city)) {
                if (location.length() > 0) location.append("-");
                location.append(city);
            }
            
            if (!isp.isEmpty() && !"null".equals(isp)) {
                if (location.length() > 0) location.append("-");
                location.append(isp);
            }
            
            String result = location.length() > 0 ? location.toString() : "未知位置";
            logger.info("IP地址 {} 的地理位置查询最终结果: {}", ipAddress, result);
            
            return result;
            
        } catch (RestClientException e) {
            logger.error("网络请求异常，IP: {}, 错误详情: {}", ipAddress, e.getMessage(), e);
            return "网络异常";
        } catch (Exception e) {
            logger.error("查询IP地理位置时发生异常，IP: {}", ipAddress, e);
            return "查询异常";
        }
    }
    
    /**
     * 判断是否为本地IP地址
     * 
     * @param ipAddress IP地址
     * @return 是否为本地IP
     */
    private boolean isLocalIp(String ipAddress) {
        if (ipAddress == null) {
            return true;
        }
        
        // 常见的本地IP地址模式
        return ipAddress.equals("127.0.0.1") ||
               ipAddress.equals("localhost") ||
               ipAddress.equals("0:0:0:0:0:0:0:1") ||
               ipAddress.equals("::1") ||
               ipAddress.startsWith("192.168.") ||
               ipAddress.startsWith("10.") ||
               ipAddress.startsWith("172.16.") ||
               ipAddress.startsWith("172.17.") ||
               ipAddress.startsWith("172.18.") ||
               ipAddress.startsWith("172.19.") ||
               ipAddress.startsWith("172.20.") ||
               ipAddress.startsWith("172.21.") ||
               ipAddress.startsWith("172.22.") ||
               ipAddress.startsWith("172.23.") ||
               ipAddress.startsWith("172.24.") ||
               ipAddress.startsWith("172.25.") ||
               ipAddress.startsWith("172.26.") ||
               ipAddress.startsWith("172.27.") ||
               ipAddress.startsWith("172.28.") ||
               ipAddress.startsWith("172.29.") ||
               ipAddress.startsWith("172.30.") ||
               ipAddress.startsWith("172.31.");
    }
}