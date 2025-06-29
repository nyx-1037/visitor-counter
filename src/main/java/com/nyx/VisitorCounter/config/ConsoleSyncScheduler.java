package com.nyx.visitorcounter.config;

import com.nyx.visitorcounter.model.Console;
import com.nyx.visitorcounter.repository.ConsoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class ConsoleSyncScheduler {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ConsoleMapper consoleMapper;

    private final String CACHE_KEY_PREFIX = "console:";

    @Scheduled(fixedRate = 600000) // 10 minutes
    public void syncRedisToMysql() {
        Set<String> redisKeys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
        if (redisKeys != null && !redisKeys.isEmpty()) {
            List<Object> consoles = redisTemplate.opsForValue().multiGet(redisKeys);
            for (Object obj : consoles) {
                if (obj instanceof Console) {
                    Console console = (Console) obj;
                    Console existingConsole = consoleMapper.selectById(console.getId());
                    if (existingConsole != null) {
                        consoleMapper.updateById(console);
                    } else {
                        consoleMapper.insert(console);
                    }
                }
            }
        }
    }
}