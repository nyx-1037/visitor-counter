package com.nyx.visitorcounter.controller;

import com.nyx.visitorcounter.model.User;
import com.nyx.visitorcounter.service.UserDetailsServiceImpl;
import com.nyx.visitorcounter.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.concurrent.TimeUnit;

/**
 * 认证控制器
 * 提供用户登录、登出等认证相关的API接口
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户登出接口
     * 从Redis中删除用户的JWT令牌，使其失效
     * 
     * @param token 请求头中的Authorization令牌
     * @return 登出成功或失败的响应
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            String username = jwtUtil.extractUsername(jwt);
            redisTemplate.delete("jwt_token:" + username);
            return ResponseEntity.ok().body("{\"message\":\"Logout successful\"}");
        }
        return ResponseEntity.badRequest().body("{\"message\":\"Invalid token\"}");
    }



    /**
     * 用户登录接口
     * 验证用户名和密码，生成JWT令牌和刷新令牌，并将JWT令牌存储在Redis中
     * 
     * @param authenticationRequest 包含用户名和密码的用户对象
     * @return 包含JWT令牌和刷新令牌的响应
     * @throws Exception 如果用户名或密码不正确
     */
    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody User authenticationRequest) throws Exception {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
            );
        } catch (Exception e) {
            throw new Exception("Incorrect username or password", e);
        }

        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(authenticationRequest.getUsername());

        final String jwt = jwtUtil.generateToken(userDetails);
        final String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        // Store token in Redis with expiration
        Long expiration = jwtUtil.extractExpiration(jwt).getTime();
        long currentTime = System.currentTimeMillis();
        long ttl = (expiration - currentTime) / 1000; // TTL in seconds

        if (ttl > 0) {
            redisTemplate.opsForValue().set("jwt_token:" + userDetails.getUsername(), jwt, ttl, TimeUnit.SECONDS);
        }

        return ResponseEntity.ok(new AuthResponse(jwt, refreshToken));
    }

    /**
     * 认证响应内部类
     * 用于封装登录成功后返回的JWT令牌和刷新令牌
     */
    static class AuthResponse {
        private final String jwt;
        private final String refreshToken;

        /**
         * 构造认证响应对象
         * 
         * @param jwt JWT访问令牌
         * @param refreshToken JWT刷新令牌
         */
        public AuthResponse(String jwt, String refreshToken) {
            this.jwt = jwt;
            this.refreshToken = refreshToken;
        }

        /**
         * 获取JWT访问令牌
         * 
         * @return JWT访问令牌
         */
        public String getJwt() {
            return jwt;
        }

        /**
         * 获取JWT刷新令牌
         * 
         * @return JWT刷新令牌
         */
        public String getRefreshToken() {
            return refreshToken;
        }
    }
}