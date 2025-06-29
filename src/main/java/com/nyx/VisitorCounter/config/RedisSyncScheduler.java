package com.nyx.visitorcounter.config;

import com.nyx.visitorcounter.model.Visitor;
import com.nyx.visitorcounter.repository.VisitorMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RedisSyncScheduler {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private VisitorMapper visitorMapper;

    private final String CACHE_KEY_PREFIX = "visitor:";

    @Scheduled(fixedRate = 600000) // 10 minutes
    public void syncRedisToMysql() {
        Set<String> redisKeys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
        if (redisKeys != null && !redisKeys.isEmpty()) {
            List<Object> visitors = redisTemplate.opsForValue().multiGet(redisKeys);
            for (Object obj : visitors) {
                if (obj instanceof Visitor) {
                    Visitor visitor = (Visitor) obj;
                    Visitor existingVisitor = visitorMapper.findByTarget(visitor.getTarget());
                    if (existingVisitor != null) {
                        visitorMapper.updateById(visitor);
                    } else {
                        visitorMapper.insert(visitor);
                    }
                }
            }
        }
    }
}