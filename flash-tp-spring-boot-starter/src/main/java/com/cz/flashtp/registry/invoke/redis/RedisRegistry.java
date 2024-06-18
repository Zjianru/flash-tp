package com.cz.flashtp.registry.invoke.redis;

import com.cz.flashtp.domain.entity.ThreadPoolConfig;
import com.cz.flashtp.domain.valobj.RegistryEnumVO;
import com.cz.flashtp.registry.Registry;
import org.redisson.api.RBucket;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.List;

/**
 * Redis 注册中心实现
 * RedisRegistry类实现了Registry接口，用于使用Redisson客户端将线程池配置信息报告给Redis。
 * 该类的主要作用是提供方法来存储和更新线程池配置以及线程池配置参数。
 *
 * @author Zjianru
 */
public class RedisRegistry implements Registry {

    /**
     * Redisson客户端，用于与Redis进行交互。
     */
    private final RedissonClient redissonClient;

    /**
     * 构造函数，初始化RedisRegistry实例。
     *
     * @param redissonClient Redisson客户端实例，用于后续操作Redis。
     */
    public RedisRegistry(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 将线程池配置信息报告给Redis。
     * 该方法首先清除已有的线程池配置列表，然后将新的线程池配置列表全部添加进去。
     *
     * @param threadPools 线程池配置列表，包含多个ThreadPoolConfig实例。
     */
    @Override
    public void reportThreadPool(List<ThreadPoolConfig> threadPools) {
        // 获取线程池配置列表的Redis键
        RList<ThreadPoolConfig> list = redissonClient.getList(RegistryEnumVO.THREAD_POOL_CONFIG_LIST_KEY.getKey());
        // 清除当前列表中的所有元素
        list.delete();
        // 将新的线程池配置列表添加到Redis中
        list.addAll(threadPools);
    }

    /**
     * 将单个线程池的配置参数报告给Redis。
     * 该方法通过构建唯一的键名来存储线程池配置参数，确保每个线程池的配置参数都能独立存储和查询。
     *
     * @param threadPoolConfig 线程池的配置参数信息。
     */
    @Override
    public void reportThreadPoolConfigParameter(ThreadPoolConfig threadPoolConfig) {
        // 构建唯一的键名，包含线程池配置参数的列表键前缀和线程池的名称与应用名称
        String cacheKey = RegistryEnumVO.THREAD_POOL_CONFIG_PARAMETER_LIST_KEY.getKey()
                + "_"
                + threadPoolConfig.getAppName()
                + "_"
                + threadPoolConfig.getThreadPoolName();
        // 获取对应的Redis键的Bucket对象
        RBucket<ThreadPoolConfig> bucket = redissonClient.getBucket(cacheKey);
        // 将线程池配置参数存储到Redis中，并设置过期时间为30天
        bucket.set(threadPoolConfig, Duration.ofDays(30));
    }

}

