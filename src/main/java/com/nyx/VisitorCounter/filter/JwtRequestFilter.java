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

/**
 * JWT请求过滤器
 * <p>
 * 该过滤器拦截所有HTTP请求，验证JWT令牌的有效性，并设置Spring Security上下文。
 * 主要功能包括：
 * <ul>
 *   <li>从请求头中提取JWT令牌</li>
 *   <li>验证令牌的有效性</li>
 *   <li>处理令牌过期的情况，使用刷新令牌生成新的访问令牌</li>
 *   <li>设置认证信息到安全上下文</li>
 * </ul>
 * <p>
 * 该过滤器与Redis集成，用于存储和验证令牌。
 */
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

    /**
     * 过滤器内部处理逻辑
     * <p>
     * 从请求头中提取JWT令牌，验证其有效性，并设置认证信息。
     * 如果访问令牌已过期但刷新令牌有效，则生成新的访问令牌。
     *
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param chain 过滤器链
     * @throws ServletException 如果处理过程中发生Servlet异常
     * @throws IOException 如果处理过程中发生I/O异常
     */
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

    /**
     * 设置认证信息到安全上下文
     * <p>
     * 创建认证令牌并将其设置到Spring Security上下文中，
     * 使当前请求被视为已认证。
     *
     * @param userDetails 用户详情对象
     * @param request HTTP请求对象
     */
    private void setAuthentication(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        usernamePasswordAuthenticationToken
                .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
    }
}