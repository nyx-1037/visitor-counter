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

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

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

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

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

    static class AuthResponse {
        private final String jwt;
        private final String refreshToken;

        public AuthResponse(String jwt, String refreshToken) {
            this.jwt = jwt;
            this.refreshToken = refreshToken;
        }

        public String getJwt() {
            return jwt;
        }

        public String getRefreshToken() {
            return refreshToken;
        }
    }
}