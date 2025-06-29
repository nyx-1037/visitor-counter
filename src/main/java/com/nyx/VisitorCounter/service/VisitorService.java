package com.nyx.visitorcounter.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.nyx.visitorcounter.model.Console;
import com.nyx.visitorcounter.model.PageResult;
import com.nyx.visitorcounter.model.Visitor;
import com.nyx.visitorcounter.repository.ConsoleMapper;
import com.nyx.visitorcounter.repository.VisitorMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;
import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;


/**
 * 访问量统计服务
 * 提供访问量记录的增加、查询、创建、更新、删除等功能
 * 使用Redis缓存提高性能，定期将数据同步到MySQL数据库
 */
@Service
public class VisitorService {

    private static final Logger logger = LoggerFactory.getLogger(VisitorService.class);

    @Autowired
    private VisitorMapper visitorMapper;

    @Autowired
    private ConsoleMapper consoleMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private ConsoleService consoleService;

    private final String CACHE_KEY_PREFIX = "visitor:";

    /**
     * 初始化Redis缓存
     * 在服务启动时将数据库中的所有访问量记录加载到Redis缓存中
     */
    @PostConstruct
    public void initCache() {
        logger.info("初始化Redis缓存，从数据库加载所有访问量记录");
        List<Visitor> visitors = visitorMapper.selectList(null);
        logger.debug("从数据库加载了{}条访问量记录", visitors.size());
        
        for (Visitor visitor : visitors) {
            redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + visitor.getTarget(), visitor);
            logger.debug("缓存访问量记录到Redis，键: {}", CACHE_KEY_PREFIX + visitor.getTarget());
        }
        logger.info("Redis缓存初始化完成，共缓存{}条记录", visitors.size());
    }

    /**
     * 增加指定目标的访问量计数
     * 从Redis缓存中获取访问量记录，增加计数并更新缓存
     * 同时创建访问日志记录并保存到Redis
     * 
     * @param target 目标标识符
     * @param request HTTP请求对象，用于获取IP地址等信息
     * @return 更新后的访问量计数，如果目标不存在或未激活则返回-1
     */
    public Long incrementVisitorCount(String target, HttpServletRequest request) {
        logger.info("增加访问量计数，目标: {}, IP: {}", target, request.getRemoteAddr());
        Object cached = redisTemplate.opsForValue().get(CACHE_KEY_PREFIX + target);
        logger.debug("缓存对象类型: {}", (cached != null ? cached.getClass().getName() : "null"));
        if (cached instanceof Visitor) {
            Visitor visitor = (Visitor) cached;
            logger.debug("访问量记录状态: {}", visitor.getStatus());
            if (visitor.getStatus() == 1) {
                visitor.setCount(visitor.getCount() + 1);
                logger.debug("增加计数后，目标: {}, 新计数: {}", target, visitor.getCount());
                
                // Only update Redis cache, actual DB sync happens via scheduler
                redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + target, visitor);
                logger.debug("更新Redis缓存，键: {}", CACHE_KEY_PREFIX + target);

                // Create console log and save to Redis only
                Console console = new Console();
                console.setVisitorId(visitor.getId());
                console.setIpAddress(request.getRemoteAddr());
                console.setCreateTime(LocalDateTime.now());
                
                // Save console to Redis via ConsoleService
                consoleService.saveConsole(console);
                logger.debug("记录控制台日志到Redis，访问量ID: {}, IP: {}", visitor.getId(), request.getRemoteAddr());

                return visitor.getCount();
            } else {
                logger.debug("访问量记录未激活，目标: {}, 状态: {}", target, visitor.getStatus());
            }
        } else if (cached == null) {
            logger.debug("Redis缓存中未找到目标: {}, 尝试从数据库获取", target);
            // Try to get from database if not in cache
            Visitor visitor = visitorMapper.findByTarget(target);
            if (visitor != null && visitor.getStatus() == 1) {
                logger.debug("从数据库获取到目标: {}, ID: {}, 计数: {}, 更新Redis缓存", 
                    target, visitor.getId(), visitor.getCount());
                redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + target, visitor);
                return incrementVisitorCount(target, request);
            } else {
                logger.debug("数据库中未找到目标: {} 或状态非激活", target);
            }
        }
        logger.debug("无法增加访问量计数，目标: {} 不存在或未激活", target);
        return -1L; // Indicate that target not found or not active
    }

    /**
     * 创建新的访问量记录
     * 如果未设置ID，则使用Redis生成临时ID
     * 仅保存到Redis缓存，实际数据库同步通过定时任务完成
     * 
     * @param visitor 要创建的访问量记录对象
     * @return 创建后的访问量记录对象
     */
    public Visitor createVisitor(Visitor visitor) {
        logger.info("创建新的访问量记录，目标: {}", visitor.getTarget());
        // Generate a temporary ID if not set
        if (visitor.getId() == null) {
            // Use Redis to generate a unique ID
            Long tempId = redisTemplate.opsForValue().increment("visitor_id_sequence");
            visitor.setId(tempId.intValue());
            logger.debug("使用Redis生成临时ID: {}", tempId);
        }
        
        // Only save to Redis, actual DB sync happens via scheduler
        redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + visitor.getTarget(), visitor);
        logger.debug("缓存新的访问量记录到Redis，目标: {}, ID: {}", visitor.getTarget(), visitor.getId());
        return visitor;
    }

    /**
     * 根据目标标识符获取访问量记录
     * 优先从Redis缓存获取，如果缓存中不存在则从数据库获取并更新缓存
     * 
     * @param target 目标标识符
     * @return 访问量记录对象，如果不存在则返回null
     */
    public Visitor getVisitorByTarget(String target) {
        logger.debug("根据目标获取访问量记录，目标: {}", target);
        Visitor visitor = (Visitor) redisTemplate.opsForValue().get(CACHE_KEY_PREFIX + target);
        if (visitor == null) {
            logger.debug("Redis缓存中未找到目标: {}, 尝试从数据库获取", target);
            visitor = visitorMapper.findByTarget(target);
            if (visitor != null) {
                logger.debug("从数据库获取到目标访问量记录，ID: {}, 目标: {}, 计数: {}", 
                    visitor.getId(), visitor.getTarget(), visitor.getCount());
                redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + target, visitor);
                logger.debug("缓存访问量记录到Redis，键: {}", CACHE_KEY_PREFIX + target);
            } else {
                logger.debug("数据库中未找到目标: {}", target);
            }
        } else {
            logger.debug("在Redis缓存中找到目标访问量记录，ID: {}, 目标: {}, 计数: {}", 
                visitor.getId(), visitor.getTarget(), visitor.getCount());
        }
        return visitor;
    }

    /**
     * 获取所有访问量记录
     * 从数据库获取所有记录并更新Redis缓存
     * 
     * @return 所有访问量记录列表
     */
    public java.util.List<Visitor> getAllVisitors() {
        logger.info("获取所有访问量记录");
        List<Visitor> dbVisitors = visitorMapper.selectList(null);
        logger.debug("从数据库获取到{}条访问量记录", dbVisitors.size());
        
        for (Visitor visitor : dbVisitors) {
            redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + visitor.getTarget(), visitor);
            logger.debug("缓存访问量记录到Redis，键: {}", CACHE_KEY_PREFIX + visitor.getTarget());
        }
        logger.info("完成获取所有访问量记录，共{}条", dbVisitors.size());
        return dbVisitors;
    }
    
    /**
     * 分页查询访问量列表，支持模糊查询
     * @param pageNum 页码
     * @param pageSize 每页记录数
     * @param target 目标（模糊查询）
     * @param description 描述（模糊查询）
     * @param status 状态
     * @return 分页结果
     */
    public PageResult<Visitor> getVisitorsByPage(int pageNum, int pageSize, String target, String description, Integer status) {
        PageHelper.startPage(pageNum, pageSize);
        List<Visitor> visitors = visitorMapper.findVisitorsByPage(target, description, status);
        PageInfo<Visitor> pageInfo = new PageInfo<>(visitors);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getPages());
    }

    /**
     * 删除指定ID的访问量记录
     * 先在Redis缓存中查找记录，如果找不到则从数据库查找
     * 仅从Redis缓存中删除，实际数据库同步通过定时任务完成
     * 
     * @param id 要删除的访问量记录ID
     */
    public void deleteVisitor(Integer id) {
        logger.info("删除访问量记录，ID: {}", id);
        // First check Redis cache
        String cacheKey = null;
        Visitor visitor = null;
        
        // Try to find the visitor in Redis by id
        Set<String> keys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
        if (keys != null) {
            logger.debug("从Redis获取到{}个访问量记录键", keys.size());
            for (String key : keys) {
                Object obj = redisTemplate.opsForValue().get(key);
                if (obj instanceof Visitor && ((Visitor) obj).getId().equals(id)) {
                    cacheKey = key;
                    logger.debug("在Redis中找到ID: {}的访问量记录，键: {}", id, key);
                    break;
                }
            }
        }
        
        // If not found in Redis, get from database
        if (cacheKey == null) {
            logger.debug("Redis缓存中未找到ID: {}的访问量记录，尝试从数据库获取", id);
            visitor = visitorMapper.selectById(id);
            if (visitor != null) {
                cacheKey = CACHE_KEY_PREFIX + visitor.getTarget();
                logger.debug("从数据库获取到ID: {}的访问量记录，目标: {}", id, visitor.getTarget());
            } else {
                logger.debug("数据库中未找到ID: {}的访问量记录", id);
            }
        }
        
        // Only delete from Redis, actual DB sync happens via scheduler
        if (cacheKey != null) {
            logger.debug("从Redis缓存删除访问量记录，键: {}", cacheKey);
            redisTemplate.delete(cacheKey);
        } else {
            logger.debug("未找到ID: {}的Redis缓存记录", id);
        }
    }

    @Autowired
    private com.nyx.visitorcounter.config.RedisSyncScheduler redisSyncScheduler;
    
    /**
     * 手动触发Redis数据同步到MySQL
     * 调用RedisSyncScheduler的批量同步方法，将Redis缓存中的访问量数据同步到MySQL数据库
     */
    public void syncRedisToMysql() {
        logger.info("手动触发Redis数据同步到MySQL");
        // 调用RedisSyncScheduler的批量同步方法
        redisSyncScheduler.syncRedisToMysqlWithBatch(30, 200);
        logger.info("Redis数据同步到MySQL完成");
    }

    /**
     * 更新访问量记录
     * 仅更新Redis缓存，实际数据库同步通过定时任务完成
     * 
     * @param visitor 要更新的访问量记录对象
     * @return 更新后的访问量记录对象
     */
    public Visitor updateVisitor(Visitor visitor) {
        logger.info("更新访问量记录，ID: {}, 目标: {}", visitor.getId(), visitor.getTarget());
        // Only update Redis, actual DB sync happens via scheduler
        redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + visitor.getTarget(), visitor);
        logger.debug("更新Redis缓存中的访问量记录，键: {}", CACHE_KEY_PREFIX + visitor.getTarget());
        return visitor;
    }

    /**
     * 更新访问量记录的状态
     * 先在Redis缓存中查找记录，如果找不到则从数据库查找
     * 仅更新Redis缓存中的状态，实际数据库同步通过定时任务完成
     * 
     * @param id 要更新状态的访问量记录ID
     * @param status 新的状态值
     */
    public void updateVisitorStatus(Integer id, Integer status) {
        logger.info("更新访问量记录状态，ID: {}, 新状态: {}", id, status);
        // First check Redis cache
        String cacheKey = null;
        Visitor visitor = null;
        
        // Try to find the visitor in Redis by id
        Set<String> keys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
        if (keys != null) {
            logger.debug("从Redis获取到{}个访问量记录键", keys.size());
            for (String key : keys) {
                Object obj = redisTemplate.opsForValue().get(key);
                if (obj instanceof Visitor && ((Visitor) obj).getId().equals(id)) {
                    visitor = (Visitor) obj;
                    cacheKey = key;
                    logger.debug("在Redis中找到ID: {}的访问量记录，键: {}, 当前状态: {}", 
                        id, key, visitor.getStatus());
                    break;
                }
            }
        }
        
        // If not found in Redis, get from database
        if (visitor == null) {
            logger.debug("Redis缓存中未找到ID: {}的访问量记录，尝试从数据库获取", id);
            visitor = visitorMapper.selectById(id);
            if (visitor != null) {
                cacheKey = CACHE_KEY_PREFIX + visitor.getTarget();
                logger.debug("从数据库获取到ID: {}的访问量记录，目标: {}, 当前状态: {}", 
                    id, visitor.getTarget(), visitor.getStatus());
            } else {
                logger.debug("数据库中未找到ID: {}的访问量记录", id);
            }
        }
        
        // Update status in Redis only
        if (visitor != null) {
            visitor.setStatus(status);
            redisTemplate.opsForValue().set(cacheKey, visitor);
            logger.debug("更新Redis缓存中的访问量记录状态，键: {}, 新状态: {}", cacheKey, status);
            logger.info("访问量记录状态更新成功，ID: {}, 新状态: {}", id, status);
        } else {
            logger.warn("无法更新访问量记录状态，ID: {}不存在", id);
        }
    }
}