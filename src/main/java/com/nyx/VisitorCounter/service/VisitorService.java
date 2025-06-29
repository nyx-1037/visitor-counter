package com.nyx.visitorcounter.service;

import com.nyx.visitorcounter.model.Console;
import com.nyx.visitorcounter.model.Visitor;
import com.nyx.visitorcounter.repository.ConsoleMapper;
import com.nyx.visitorcounter.repository.VisitorMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;
import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;


@Service
public class VisitorService {

    @Autowired
    private VisitorMapper visitorMapper;

    @Autowired
    private ConsoleMapper consoleMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final String CACHE_KEY_PREFIX = "visitor:";

    @PostConstruct
    public void initCache() {
        List<Visitor> visitors = visitorMapper.selectList(null);
        for (Visitor visitor : visitors) {
            redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + visitor.getTarget(), visitor);
        }
    }

    public Long incrementVisitorCount(String target, HttpServletRequest request) {
        Object cached = redisTemplate.opsForValue().get(CACHE_KEY_PREFIX + target);
        System.out.println("Cached object type: " + (cached != null ? cached.getClass().getName() : "null"));
        if (cached instanceof Visitor) {
            Visitor visitor = (Visitor) cached;
            System.out.println("Visitor status: " + visitor.getStatus());
            if (visitor.getStatus() == 1) {
                visitor.setCount(visitor.getCount() + 1);
                
                // Update both database and cache
                visitorMapper.updateById(visitor);
                redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + target, visitor);

                Console console = new Console();
                console.setVisitorId(visitor.getId());
                console.setIpAddress(request.getRemoteAddr());
                console.setCreateTime(LocalDateTime.now());
                consoleMapper.insert(console);

                return visitor.getCount();
            }
        } else if (cached == null) {
            // Try to get from database if not in cache
            Visitor visitor = visitorMapper.findByTarget(target);
            if (visitor != null && visitor.getStatus() == 1) {
                redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + target, visitor);
                return incrementVisitorCount(target, request);
            }
        }
        return -1L; // Indicate that target not found or not active
    }

    public Visitor createVisitor(Visitor visitor) {
        visitorMapper.insert(visitor);
        redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + visitor.getTarget(), visitor);
        return visitor;
    }

    public Visitor getVisitorByTarget(String target) {
        Visitor visitor = (Visitor) redisTemplate.opsForValue().get(CACHE_KEY_PREFIX + target);
        if (visitor == null) {
            visitor = visitorMapper.findByTarget(target);
            if (visitor != null) {
                redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + target, visitor);
            }
        }
        return visitor;
    }

    public java.util.List<Visitor> getAllVisitors() {
        List<Visitor> dbVisitors = visitorMapper.selectList(null);
        for (Visitor visitor : dbVisitors) {
            redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + visitor.getTarget(), visitor);
        }
        return dbVisitors;
    }

    public void deleteVisitor(Integer id) {
        Visitor visitor = visitorMapper.selectById(id);
        if (visitor != null) {
            visitorMapper.deleteById(id);
            redisTemplate.delete(CACHE_KEY_PREFIX + visitor.getTarget());
        }
    }

    public void syncRedisToMysql() {
        // 获取所有 Redis 中的 visitor key
        java.util.Set<String> keys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            for (String key : keys) {
                Object cached = redisTemplate.opsForValue().get(key);
                if (cached instanceof Visitor) {
                    Visitor visitor = (Visitor) cached;
                    // 更新数据库
                    visitorMapper.updateById(visitor);
                }
            }
        }
    }

    public Visitor updateVisitor(Visitor visitor) {
        visitorMapper.updateById(visitor);
        redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + visitor.getTarget(), visitor);
        return visitor;
    }

    public void updateVisitorStatus(Integer id, Integer status) {
        Visitor visitor = visitorMapper.selectById(id);
        if (visitor != null) {
            visitor.setStatus(status);
            visitorMapper.updateById(visitor);
            redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + visitor.getTarget(), visitor);
        }
    }
}