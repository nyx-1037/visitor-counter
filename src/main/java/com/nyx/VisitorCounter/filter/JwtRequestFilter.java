package com.nyx.visitorcounter.filter;

import com.nyx.visitorcounter.service.UserDetailsServiceImpl;
import com.nyx.visitorcounter.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.data.redis.core.RedisTemplate;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String AUTH_HEADER = "Authorization";
    private static final String REFRESH_HEADER = "Refresh-Token";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader(AUTH_HEADER);
        final String refreshToken = request.getHeader(REFRESH_HEADER);

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            username = jwtUtil.extractUsername(jwt);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            try {
                if (jwtUtil.validateToken(jwt, userDetails, redisTemplate)) {
                    setAuthentication(userDetails, request);
                }
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                // If access token is expired but refresh token is valid
                if (refreshToken != null && jwtUtil.validateRefreshToken(refreshToken, userDetails)) {
                    // Generate new access token
                    String newAccessToken = jwtUtil.generateToken(userDetails);
                    // Store new token in Redis
                    redisTemplate.opsForValue().set("jwt_token:" + username, newAccessToken);
                    // Add new token to response header
                    response.setHeader(AUTH_HEADER, "Bearer " + newAccessToken);
                    setAuthentication(userDetails, request);
                }
            }
        }
        chain.doFilter(request, response);
    }

    private void setAuthentication(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        usernamePasswordAuthenticationToken
                .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
    }
}