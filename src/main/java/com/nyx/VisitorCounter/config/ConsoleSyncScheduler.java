package com.nyx.visitorcounter.config;

import com.nyx.visitorcounter.model.Console;
import com.nyx.visitorcounter.repository.ConsoleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;


/**
 * 控制台日志Redis同步调度器，负责定期将Redis缓存中的控制台日志数据同步到MySQL数据库
 * <p>
 * 该类通过定时任务机制，定期将Redis中的控制台日志数据批量同步到MySQL数据库，
 * 以确保数据的持久化存储。同步过程支持批量处理和间隔执行，以减少对系统资源的占用。
 */
@Component
public class ConsoleSyncScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleSyncScheduler.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ConsoleMapper consoleMapper;

    private final String CACHE_KEY_PREFIX = "console:";

    /**
     * 定时同步Redis数据到MySQL数据库
     * <p>
     * 该方法通过Spring的@Scheduled注解实现定时执行，默认每10分钟执行一次。
     * 调用批量同步方法，每批处理30条记录，批次间隔200毫秒。
     */
    @Scheduled(fixedRate = 600000) // 10 minutes
    public void syncRedisToMysql() {
        logger.info("开始执行定时Redis控制台日志同步到MySQL任务");
        syncRedisToMysqlWithBatch(30, 200);
        logger.info("完成定时Redis控制台日志同步到MySQL任务");
    }
    
    /**
     * 批量同步Redis数据到MySQL
     * @param batchSize 每批次处理的数据量
     * @param intervalMs 每批次处理的间隔时间(毫秒)
     */
    public void syncRedisToMysqlWithBatch(int batchSize, long intervalMs) {
        logger.info("开始批量同步Redis控制台日志到MySQL，批次大小: {}，间隔: {}ms", batchSize, intervalMs);
        Set<String> redisKeys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
        if (redisKeys != null && !redisKeys.isEmpty()) {
            logger.debug("从Redis获取到{}个控制台日志键", redisKeys.size());
            List<String> keysList = new ArrayList<>(redisKeys);
            int totalKeys = keysList.size();
            int processedCount = 0;
            
            while (processedCount < totalKeys) {
                int endIndex = Math.min(processedCount + batchSize, totalKeys);
                List<String> batchKeys = keysList.subList(processedCount, endIndex);
                
                logger.debug("处理第{}批数据，本批次包含{}个键", (processedCount / batchSize) + 1, batchKeys.size());
                List<Object> consoles = redisTemplate.opsForValue().multiGet(batchKeys);
                if (consoles != null) {
                    logger.debug("成功从Redis获取{}个控制台日志对象", consoles.size());
                    for (Object obj : consoles) {
                        if (obj instanceof Console) {
                            Console console = (Console) obj;
                            // 保存原始ID用于后续处理
                            Integer originalId = console.getId();
                            // 设置ID为null，让数据库自动生成，确保每次都作为新记录插入
                            console.setId(null);
                            // 插入记录
                            consoleMapper.insert(console);
                            // 获取新生成的ID
                            Integer newId = console.getId();
                            logger.debug("插入新的控制台日志到MySQL，原ID: {}, 新ID: {}, IP: {}, 访问量ID: {}", 
                                originalId, newId, console.getIpAddress(), console.getVisitorId());
                            
                            // 删除旧的Redis记录
                            redisTemplate.delete(CACHE_KEY_PREFIX + originalId);
                            logger.debug("删除Redis中的旧控制台日志记录，键: {}", CACHE_KEY_PREFIX + originalId);
                            // 使用新ID保存到Redis
                            redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + newId, console, 10, TimeUnit.MINUTES);
                            logger.debug("使用新ID更新Redis中的控制台日志记录，键: {}", CACHE_KEY_PREFIX + newId);
                        }
                    }
                }
                
                processedCount = endIndex;
                
                // 如果还有数据需要处理，则等待指定的间隔时间
                if (processedCount < totalKeys) {
                    try {
                        logger.debug("批次处理完成，等待{}ms后处理下一批", intervalMs);
                        Thread.sleep(intervalMs);
                    } catch (InterruptedException e) {
                        logger.error("同步过程被中断", e);
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            logger.info("完成批量同步Redis控制台日志到MySQL，共处理{}个记录", totalKeys);
        } else {
            logger.info("Redis中没有控制台日志需要同步");
        }
    }
}