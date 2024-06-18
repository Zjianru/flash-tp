package com.cz.flashtp.config;

import com.cz.flashtp.domain.DynamicThreadPoolService;
import com.cz.flashtp.domain.entity.ThreadPoolConfig;
import com.cz.flashtp.domain.invoker.DefaultThreadPoolService;
import com.cz.flashtp.domain.valobj.RegistryEnumVO;
import com.cz.flashtp.registry.Registry;
import com.cz.flashtp.registry.invoke.redis.RedisRegistry;
import com.cz.flashtp.trigger.job.ThreadPoolDataReportJob;
import com.cz.flashtp.trigger.listener.ThreadPoolConfigAdjustListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * flash-tp 自动配置信息
 * 动态线程池自动配置类，用于在Spring应用程序中配置和管理动态线程池。
 * 该类利用Spring Boot的自动配置能力，根据配置属性初始化并管理Redisson客户端，
 * 动态线程池服务，注册表和相关监听器。
 *
 * @author Zjianru
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(FlashTpAutoConfigProperties.class)
@Slf4j
public class FlashTpAutoConfig {

    /**
     * 初始化Redisson客户端，用于后续动态线程池的配置和管理。
     *
     * @param properties 动态线程池的配置属性，包括Redis连接信息和线程池参数。
     * @return Redisson客户端实例。
     */
    @Bean
    public RedissonClient redissonClient(FlashTpAutoConfigProperties properties) {
        RedissonClient redissonClient = wrapperRedisClient(properties);
        log.info("动态线程池，注册器（redis）链接初始化完成。{} {} {}", properties.getHost(), properties.getPoolSize(), !redissonClient.isShutdown());
        return redissonClient;
    }

    /**
     * 创建动态线程池服务，用于管理应用中的线程池。
     *
     * @param applicationContext  Spring应用上下文，用于获取应用名称。
     * @param threadPoolExecutors 线程池执行器的映射，键为线程池名称，值为线程池实例。
     * @param redissonClient      Redisson客户端，用于存储和获取线程池配置。
     * @return 动态线程池服务实例。
     */
    @Bean
    public DynamicThreadPoolService flashTpService(ApplicationContext applicationContext, Map<String, ThreadPoolExecutor> threadPoolExecutors, RedissonClient redissonClient) {
        String applicationName = getApplicationName(applicationContext);
        if (StringUtils.isBlank(applicationName)) {
            applicationName = "flash-tp-defaultApp";
            log.warn("[flash-tp]==> applicationName is null and will given after process default application name");
        }
        log.info("current get thread pool info is {}", threadPoolExecutors.keySet());
        // 获取缓存数据，设置本地线程池配置
        Set<String> keys = threadPoolExecutors.keySet();
        for (String key : keys) {
            ThreadPoolConfig config = redissonClient
                    .<ThreadPoolConfig>getBucket(
                            RegistryEnumVO.THREAD_POOL_CONFIG_PARAMETER_LIST_KEY.getKey()
                                    + "_" + applicationName
                                    + "_" + key)
                    .get();
            if (null == config) continue;
            ThreadPoolExecutor threadPool = threadPoolExecutors.get(key);
            threadPool.setCorePoolSize(config.getCorePoolSize());
            threadPool.setMaximumPoolSize(config.getMaximumPoolSize());
        }
        return new DefaultThreadPoolService(applicationName, threadPoolExecutors);
    }

    /**
     * 创建注册表，用于存储和管理应用相关的元数据。
     *
     * @param redissonClient Redisson客户端，用于存储注册表数据。
     * @return 注册表实例。
     */
    @Bean
    public Registry redisRegistry(RedissonClient redissonClient) {
        return new RedisRegistry(redissonClient);
    }

    /**
     * 创建线程池数据报告任务，定期报告线程池的使用情况。
     *
     * @param dynamicThreadPoolService 动态线程池服务，用于获取线程池数据。
     * @param registry                 注册表，用于存储线程池数据。
     * @return 线程池数据报告任务实例。
     */
    @Bean
    public ThreadPoolDataReportJob threadPoolDataReportJob(DynamicThreadPoolService dynamicThreadPoolService, Registry registry) {
        return new ThreadPoolDataReportJob(dynamicThreadPoolService, registry);
    }

    /**
     * 创建线程池配置调整监听器，用于监听并应用线程池配置的变更。
     *
     * @param dynamicThreadPoolService 动态线程池服务，用于应用配置变更。
     * @param registry                 注册表，用于存储配置数据。
     * @return 线程池配置调整监听器实例。
     */
    @Bean
    public ThreadPoolConfigAdjustListener threadPoolConfigAdjustListener(DynamicThreadPoolService dynamicThreadPoolService, Registry registry) {
        return new ThreadPoolConfigAdjustListener(dynamicThreadPoolService, registry);
    }

    /**
     * 创建用于监听线程池配置调整的Redis主题，并订阅该主题。
     *
     * @param applicationContext             Spring应用上下文，用于获取应用名称。
     * @param redissonClient                 Redisson客户端，用于创建和管理Redis主题。
     * @param threadPoolConfigAdjustListener 线程池配置调整监听器，用于处理配置变更事件。
     * @return Redis主题实例。
     */
    @Bean(name = "dynamicThreadPoolRedisTopic")
    public RTopic threadPoolConfigAdjustListener(ApplicationContext applicationContext, RedissonClient redissonClient, ThreadPoolConfigAdjustListener threadPoolConfigAdjustListener) {
        RTopic topic = redissonClient.getTopic(RegistryEnumVO.DYNAMIC_THREAD_POOL_REDIS_TOPIC.getKey() + "_" + getApplicationName(applicationContext));
        topic.addListener(ThreadPoolConfig.class, threadPoolConfigAdjustListener);
        return topic;
    }


    /**
     * 获取应用名称，用于标识和区分不同的Spring应用程序。
     *
     * @param applicationContext Spring应用上下文。
     * @return 应用名称。
     */
    private String getApplicationName(ApplicationContext applicationContext) {
        return applicationContext.getEnvironment().getProperty("spring.application.name");
    }

    /**
     * 初始化并配置Redisson客户端。
     *
     * @param properties 动态线程池的配置属性，包括Redis连接信息和线程池参数。
     * @return 配置好的Redisson客户端实例。
     */
    private RedissonClient wrapperRedisClient(FlashTpAutoConfigProperties properties) {
        Config config = new Config();
        // 根据需要可以设定编解码器；https://github.com/redisson/redisson/wiki/4.-%E6%95%B0%E6%8D%AE%E5%BA%8F%E5%88%97%E5%8C%96
        config.setCodec(JsonJacksonCodec.INSTANCE);
        config.useSingleServer()
                .setAddress("redis://" + properties.getHost() + ":" + properties.getPort())
                .setPassword(properties.getPassword())
                .setConnectionPoolSize(properties.getPoolSize())
                .setConnectionMinimumIdleSize(properties.getMinIdleSize())
                .setIdleConnectionTimeout(properties.getIdleTimeout())
                .setConnectTimeout(properties.getConnectTimeout())
                .setRetryAttempts(properties.getRetryAttempts())
                .setRetryInterval(properties.getRetryInterval())
                .setPingConnectionInterval(properties.getPingInterval())
                .setKeepAlive(properties.isKeepAlive());
        return Redisson.create(config);
    }
}