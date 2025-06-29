package com.nyx.visitorcounter.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.nyx.visitorcounter.model.Console;
import com.nyx.visitorcounter.model.PageResult;
import com.nyx.visitorcounter.repository.ConsoleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import java.time.LocalDateTime;
import java.util.List;


/**
 * 访问日志服务
 * 提供访问日志记录的保存、查询、更新、删除等功能
 * 使用Redis缓存提高性能，定期将数据同步到MySQL数据库
 */
@Service
public class ConsoleService {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleService.class);

    @Autowired
    private ConsoleMapper consoleMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final String CACHE_KEY_PREFIX = "console:";

    /**
     * 保存访问日志记录
     * 如果未设置ID，则使用Redis生成临时ID
     * 仅保存到Redis缓存，实际数据库同步通过定时任务完成
     * 缓存有效期为10分钟
     * 
     * @param console 要保存的访问日志记录对象
     * @return 保存后的访问日志记录对象
     */
    public Console saveConsole(Console console) {
        logger.debug("保存控制台日志，访问量ID: {}, IP: {}", console.getVisitorId(), console.getIpAddress());
        // Generate a temporary ID if not set
        if (console.getId() == null) {
            // Use Redis to generate a unique ID
            Long tempId = redisTemplate.opsForValue().increment("console_id_sequence");
            console.setId(tempId.intValue());
            logger.debug("使用Redis生成临时ID: {}", tempId);
        }
        
        // Only cache to Redis, actual DB sync happens via scheduler
        redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + console.getId(), console, 10, TimeUnit.MINUTES); // Cache for 10 minutes
        logger.debug("缓存控制台日志到Redis，键: {}, 有效期: 10分钟", CACHE_KEY_PREFIX + console.getId());
        return console;
    }

    /**
     * 根据ID获取访问日志记录
     * 优先从Redis缓存获取，如果缓存中不存在则从数据库获取并更新缓存
     * 缓存有效期为10分钟
     * 
     * @param id 访问日志记录ID
     * @return 访问日志记录对象，如果不存在则返回null
     */
    public Console getConsoleById(Integer id) {
        logger.debug("获取控制台日志，ID: {}", id);
        Console console = (Console) redisTemplate.opsForValue().get(CACHE_KEY_PREFIX + id);
        if (console == null) {
            logger.debug("Redis缓存中未找到ID: {}的控制台日志，尝试从数据库获取", id);
            console = consoleMapper.selectById(id);
            if (console != null) {
                logger.debug("从数据库获取到ID: {}的控制台日志，访问量ID: {}, IP: {}", 
                    id, console.getVisitorId(), console.getIpAddress());
                redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + id, console, 10, TimeUnit.MINUTES);
                logger.debug("缓存控制台日志到Redis，键: {}, 有效期: 10分钟", CACHE_KEY_PREFIX + id);
            } else {
                logger.debug("数据库中未找到ID: {}的控制台日志", id);
            }
        } else {
            logger.debug("从Redis缓存获取到ID: {}的控制台日志，访问量ID: {}, IP: {}", 
                id, console.getVisitorId(), console.getIpAddress());
        }
        return console;
    }

    /**
     * 获取所有访问日志记录
     * 优先从Redis缓存获取，如果缓存中不存在或为空则从数据库获取
     * 
     * @return 所有访问日志记录列表
     */
    public List<Console> getAllConsoles() {
        logger.debug("获取所有控制台日志");
        Set<String> keys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
        logger.debug("从Redis获取到{}个控制台日志键", keys != null ? keys.size() : 0);
        
        List<Object> consoles = keys != null ? redisTemplate.opsForValue().multiGet(keys) : null;
        if (consoles != null && !consoles.isEmpty()) {
            logger.debug("从Redis缓存获取到{}个控制台日志对象", consoles.size());
            List<Console> consoleList = new java.util.ArrayList<>();
            for (Object obj : consoles) {
                if (obj instanceof Console) {
                    consoleList.add((Console) obj);
                }
            }
            logger.debug("成功转换{}个控制台日志对象", consoleList.size());
            return consoleList;
        } else {
            logger.debug("Redis缓存中未找到控制台日志，从数据库获取");
            List<Console> result = consoleMapper.selectList(null);
            logger.debug("从数据库获取到{}个控制台日志", result.size());
            return result;
        }
    }
    
    /**
     * 分页查询日志列表，支持模糊查询
     * @param pageNum 页码
     * @param pageSize 每页记录数
     * @param visitorId 访问量ID
     * @param ipAddress IP地址（模糊查询）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 分页结果
     */
    public PageResult<Console> getConsolesByPage(int pageNum, int pageSize, Integer visitorId, 
                                               String ipAddress, LocalDateTime startTime, LocalDateTime endTime) {
        PageHelper.startPage(pageNum, pageSize);
        List<Console> consoles = consoleMapper.findConsolesByPage(visitorId, ipAddress, startTime, endTime);
        PageInfo<Console> pageInfo = new PageInfo<>(consoles);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getPages());
    }

    /**
     * 删除指定ID的访问日志记录
     * 同时从数据库和Redis缓存中删除
     * 
     * @param id 要删除的访问日志记录ID
     */
    public void deleteConsole(Integer id) {
        logger.info("删除控制台日志，ID: {}", id);
        consoleMapper.deleteById(id);
        logger.debug("从数据库删除控制台日志，ID: {}", id);
        redisTemplate.delete(CACHE_KEY_PREFIX + id);
        logger.debug("从Redis缓存删除控制台日志，键: {}", CACHE_KEY_PREFIX + id);
    }

    /**
     * 更新访问日志记录
     * 同时更新数据库和Redis缓存
     * 缓存有效期为10分钟
     * 
     * @param console 要更新的访问日志记录对象
     * @return 更新后的访问日志记录对象
     */
    public Console updateConsole(Console console) {
        logger.info("更新控制台日志，ID: {}, 访问量ID: {}", console.getId(), console.getVisitorId());
        consoleMapper.updateById(console);
        logger.debug("更新数据库中的控制台日志，ID: {}", console.getId());
        redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + console.getId(), console, 10, TimeUnit.MINUTES);
        logger.debug("更新Redis缓存中的控制台日志，键: {}, 有效期: 10分钟", CACHE_KEY_PREFIX + console.getId());
        return console;
    }
    
    @Autowired
    private com.nyx.visitorcounter.config.ConsoleSyncScheduler consoleSyncScheduler;
    
    /**
     * 手动触发Redis数据同步到MySQL
     * 调用ConsoleSyncScheduler的批量同步方法，将Redis缓存中的访问日志数据同步到MySQL数据库
     * 批量处理每批30条记录，最多处理200条
     */
    public void syncRedisToMysql() {
        logger.info("手动触发Redis控制台日志同步到MySQL");
        // 调用ConsoleSyncScheduler的批量同步方法
        consoleSyncScheduler.syncRedisToMysqlWithBatch(30, 200);
        logger.info("完成手动触发Redis控制台日志同步");
    }
}