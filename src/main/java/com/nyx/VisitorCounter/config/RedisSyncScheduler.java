package com.nyx.visitorcounter.config;

import com.nyx.visitorcounter.model.Visitor;
import com.nyx.visitorcounter.repository.VisitorMapper;
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
import java.util.stream.Collectors;

/**
 * Redis同步调度器，负责定期将Redis缓存中的访问量数据同步到MySQL数据库
 * <p>
 * 该类通过定时任务机制，定期将Redis中的访问量数据批量同步到MySQL数据库，
 * 以确保数据的持久化存储。同步过程支持批量处理和间隔执行，以减少对系统资源的占用。
 */
@Component
public class RedisSyncScheduler {

    private static final Logger logger = LoggerFactory.getLogger(RedisSyncScheduler.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private VisitorMapper visitorMapper;

    private final String CACHE_KEY_PREFIX = "visitor:";

    /**
     * 定时同步Redis数据到MySQL数据库
     * <p>
     * 该方法通过Spring的@Scheduled注解实现定时执行，默认每10分钟执行一次。
     * 调用批量同步方法，每批处理30条记录，批次间隔200毫秒。
     */
    @Scheduled(fixedRate = 600000) // 10 minutes
    public void syncRedisToMysql() {
        logger.info("开始执行定时Redis访问量数据同步到MySQL任务");
        syncRedisToMysqlWithBatch(30, 200);
        logger.info("完成定时Redis访问量数据同步到MySQL任务");
    }
    
    /**
     * 批量同步Redis数据到MySQL
     * @param batchSize 每批次处理的数据量
     * @param intervalMs 每批次处理的间隔时间(毫秒)
     */
    public void syncRedisToMysqlWithBatch(int batchSize, long intervalMs) {
        logger.info("开始批量同步Redis访问量数据到MySQL，批次大小: {}，间隔: {}ms", batchSize, intervalMs);
        Set<String> redisKeys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
        if (redisKeys != null && !redisKeys.isEmpty()) {
            logger.debug("从Redis获取到{}个访问量记录键", redisKeys.size());
            List<String> keysList = new ArrayList<>(redisKeys);
            int totalKeys = keysList.size();
            int processedCount = 0;
            
            while (processedCount < totalKeys) {
                int endIndex = Math.min(processedCount + batchSize, totalKeys);
                List<String> batchKeys = keysList.subList(processedCount, endIndex);
                
                logger.debug("处理第{}批数据，本批次包含{}个键", (processedCount / batchSize) + 1, batchKeys.size());
                List<Object> visitors = redisTemplate.opsForValue().multiGet(batchKeys);
                if (visitors != null) {
                    logger.debug("成功从Redis获取{}个访问量记录对象", visitors.size());
                    for (Object obj : visitors) {
                        if (obj instanceof Visitor) {
                            Visitor visitor = (Visitor) obj;
                            Visitor existingVisitor = visitorMapper.findByTarget(visitor.getTarget());
                            if (existingVisitor != null) {
                                // 已存在，更新记录
                                // 保留数据库中的ID
                                visitor.setId(existingVisitor.getId());
                                visitorMapper.updateById(visitor);
                                logger.debug("更新MySQL中的访问量记录，目标: {}, ID: {}, 计数: {}", 
                                    visitor.getTarget(), visitor.getId(), visitor.getCount());
                                // 更新Redis中的记录，确保使用正确的ID
                                redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + visitor.getTarget(), visitor);
                                logger.debug("更新Redis中的访问量记录，键: {}", CACHE_KEY_PREFIX + visitor.getTarget());
                            } else {
                                // 不存在，插入新记录
                                // 设置ID为null，让数据库自动生成
                                visitor.setId(null);
                                visitorMapper.insert(visitor);
                                logger.debug("插入新的访问量记录到MySQL，目标: {}, 新ID: {}, 计数: {}", 
                                    visitor.getTarget(), visitor.getId(), visitor.getCount());
                                // 更新Redis中的记录，确保使用正确的ID
                                redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + visitor.getTarget(), visitor);
                                logger.debug("更新Redis中的访问量记录，键: {}", CACHE_KEY_PREFIX + visitor.getTarget());
                            }
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
            logger.info("完成批量同步Redis访问量数据到MySQL，共处理{}个记录", totalKeys);
        } else {
            logger.info("Redis中没有访问量数据需要同步");
        }
    }
}