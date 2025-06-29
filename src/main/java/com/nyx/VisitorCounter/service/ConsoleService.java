package com.nyx.visitorcounter.service;

import com.nyx.visitorcounter.model.Console;
import com.nyx.visitorcounter.repository.ConsoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.concurrent.TimeUnit;

import java.util.List;


@Service
public class ConsoleService {

    @Autowired
    private ConsoleMapper consoleMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final String CACHE_KEY_PREFIX = "console:";

    public Console saveConsole(Console console) {
        // Only cache to Redis, actual DB sync happens via scheduler
        redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + console.getId(), console, 10, TimeUnit.MINUTES); // Cache for 10 minutes
        return console;
    }

    public Console getConsoleById(Integer id) {
        Console console = (Console) redisTemplate.opsForValue().get(CACHE_KEY_PREFIX + id);
        if (console == null) {
            console = consoleMapper.selectById(id);
            if (console != null) {
                redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + id, console, 10, TimeUnit.MINUTES);
            }
        }
        return console;
    }

    public List<Console> getAllConsoles() {
        List<Object> consoles = redisTemplate.opsForValue().multiGet(redisTemplate.keys(CACHE_KEY_PREFIX + "*"));
        if (consoles != null && !consoles.isEmpty()) {
            List<Console> consoleList = new java.util.ArrayList<>();
            for (Object obj : consoles) {
                if (obj instanceof Console) {
                    consoleList.add((Console) obj);
                }
            }
            return consoleList;
        } else {
            return consoleMapper.selectList(null);
        }
    }

    public void deleteConsole(Integer id) {
        consoleMapper.deleteById(id);
        redisTemplate.delete(CACHE_KEY_PREFIX + id);
    }

    public Console updateConsole(Console console) {
        consoleMapper.updateById(console);
        redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + console.getId(), console, 10, TimeUnit.MINUTES);
        return console;
    }
}