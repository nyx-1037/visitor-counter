package com.nyx.visitorcounter.config;

import com.nyx.visitorcounter.filter.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security配置类
 * <p>
 * 该类负责配置应用的安全策略，包括：
 * <ul>
 *   <li>认证管理器配置</li>
 *   <li>HTTP安全配置</li>
 *   <li>密码编码器配置</li>
 *   <li>JWT过滤器集成</li>
 *   <li>Redis模板配置</li>
 * </ul>
 * <p>
 * 安全配置采用无状态会话管理策略，使用JWT进行身份验证，并将令牌存储在Redis中。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    /**
     * 配置认证管理器
     * <p>
     * 设置用户详情服务和密码编码器，用于用户认证过程。
     *
     * @param auth 认证管理器构建器
     * @throws Exception 如果配置过程中发生错误
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    /**
     * 配置HTTP安全策略
     * <p>
     * 设置请求授权规则、会话管理策略和JWT过滤器。
     * 禁用CSRF保护，允许公共资源访问，要求其他请求进行身份验证，
     * 并配置无状态会话管理。
     *
     * @param http HTTP安全配置对象
     * @throws Exception 如果配置过程中发生错误
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
                .authorizeRequests()
                .antMatchers("/", "/index.html", "/admin.html", "/usage_example.html", "/embed_example.html", 
                        "/css/**", "/js/**", "/res/**","/images/**", "/api/auth/**", "/api/visitor/increment").permitAll()
                .anyRequest().authenticated()
                .and().sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
    }

    /**
     * 创建密码编码器Bean
     * <p>
     * 使用BCrypt算法进行密码加密，提供安全的密码存储和验证机制。
     *
     * @return BCrypt密码编码器实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 创建认证管理器Bean
     * <p>
     * 暴露认证管理器作为Spring Bean，以便在其他组件中使用。
     *
     * @return 认证管理器实例
     * @throws Exception 如果创建过程中发生错误
     */
    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * 创建Redis模板Bean
     * <p>
     * 配置Redis操作模板，设置连接工厂和序列化器。
     * 使用StringRedisSerializer进行键序列化，
     * 使用Jackson2JsonRedisSerializer进行值序列化，
     * 支持对象类型信息的保存和恢复。
     *
     * @param connectionFactory Redis连接工厂
     * @return 配置好的RedisTemplate实例
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key serialization
        template.setKeySerializer(new StringRedisSerializer());
        // Value serialization
        Jackson2JsonRedisSerializer<Object> jsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.NON_FINAL, com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY);
        
        // 添加Java 8日期时间模块支持
        om.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        
        jsonRedisSerializer.setObjectMapper(om);
        template.setValueSerializer(jsonRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }
}