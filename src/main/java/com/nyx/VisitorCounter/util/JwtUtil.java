package com.nyx.visitorcounter.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * JWT工具类
 * <p>
 * 提供JWT令牌的生成、解析和验证功能。主要功能包括：
 * <ul>
 *   <li>从令牌中提取用户名和过期时间</li>
 *   <li>生成访问令牌和刷新令牌</li>
 *   <li>验证令牌的有效性</li>
 *   <li>与Redis集成，用于令牌存储和验证</li>
 * </ul>
 * <p>
 * 该类使用配置文件中的密钥和过期时间设置。
 */
@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    /**
     * 从令牌中提取用户名
     *
     * @param token JWT令牌
     * @return 令牌中的用户名
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 从令牌中提取过期时间
     *
     * @param token JWT令牌
     * @return 令牌的过期时间
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 从令牌中提取指定的声明
     *
     * @param token JWT令牌
     * @param claimsResolver 声明解析函数
     * @return 解析后的声明值
     * @param <T> 声明值的类型
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 从令牌中提取所有声明
     *
     * @param token JWT令牌
     * @return 令牌中的所有声明
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    /**
     * 检查令牌是否已过期
     *
     * @param token JWT令牌
     * @return 如果令牌已过期则返回true，否则返回false
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * 为用户生成访问令牌
     *
     * @param userDetails 用户详情
     * @return 生成的JWT访问令牌
     */
    public String generateToken(UserDetails userDetails) {
        logger.debug("为用户生成访问令牌: {}", userDetails.getUsername());
        Map<String, Object> claims = new HashMap<>();
        String token = createToken(claims, userDetails.getUsername(), expiration);
        logger.debug("访问令牌生成成功，有效期: {}毫秒", expiration);
        return token;
    }

    /**
     * 为用户生成刷新令牌
     *
     * @param userDetails 用户详情
     * @return 生成的JWT刷新令牌
     */
    public String generateRefreshToken(UserDetails userDetails) {
        logger.debug("为用户生成刷新令牌: {}", userDetails.getUsername());
        Map<String, Object> claims = new HashMap<>();
        String refreshToken = createToken(claims, userDetails.getUsername(), refreshExpiration);
        logger.debug("刷新令牌生成成功，有效期: {}毫秒", refreshExpiration);
        return refreshToken;
    }

    /**
     * 创建JWT令牌
     *
     * @param claims 令牌中包含的声明
     * @param subject 令牌的主题（通常是用户名）
     * @param expirationTime 令牌的过期时间（毫秒）
     * @return 创建的JWT令牌
     */
    private String createToken(Map<String, Object> claims, String subject, Long expirationTime) {
        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SignatureAlgorithm.HS256, secret).compact();
    }

    /**
     * 验证访问令牌的有效性
     * <p>
     * 检查令牌中的用户名是否与提供的用户详情匹配，
     * 令牌是否未过期，以及令牌是否存在于Redis中。
     *
     * @param token JWT令牌
     * @param userDetails 用户详情
     * @param redisTemplate Redis操作模板
     * @return 如果令牌有效则返回true，否则返回false
     */
    public Boolean validateToken(String token, UserDetails userDetails, RedisTemplate<String, String> redisTemplate) {
        logger.debug("验证访问令牌");
        final String username = extractUsername(token);
        logger.debug("从令牌中提取的用户名: {}", username);
        
        // Check if token exists in Redis
        String redisKey = "jwt_token:" + username;
        logger.debug("从Redis获取令牌，键: {}", redisKey);
        String storedToken = redisTemplate.opsForValue().get(redisKey);
        
        boolean isUsernameValid = username.equals(userDetails.getUsername());
        boolean isNotExpired = !isTokenExpired(token);
        boolean isTokenMatch = token.equals(storedToken);
        
        logger.debug("令牌验证结果 - 用户名匹配: {}, 未过期: {}, 与Redis中的令牌匹配: {}", 
                isUsernameValid, isNotExpired, isTokenMatch);
        
        return (isUsernameValid && isNotExpired && isTokenMatch);
    }

    /**
     * 验证刷新令牌的有效性
     * <p>
     * 检查令牌中的用户名是否与提供的用户详情匹配，
     * 以及令牌是否未过期。
     *
     * @param token JWT刷新令牌
     * @param userDetails 用户详情
     * @return 如果刷新令牌有效则返回true，否则返回false
     */
    public Boolean validateRefreshToken(String token, UserDetails userDetails) {
        logger.debug("验证刷新令牌");
        final String username = extractUsername(token);
        logger.debug("从刷新令牌中提取的用户名: {}", username);
        
        boolean isUsernameValid = username.equals(userDetails.getUsername());
        boolean isNotExpired = !isTokenExpired(token);
        
        logger.debug("刷新令牌验证结果 - 用户名匹配: {}, 未过期: {}", isUsernameValid, isNotExpired);
        
        return (isUsernameValid && isNotExpired);
    }
}