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
 *
 * @author Zjianru
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(FlashTpAutoConfigProperties.class)
@Slf4j
public class FlashTpAutoConfig {


    @Bean
    public RedissonClient redissonClient(FlashTpAutoConfigProperties properties) {
        RedissonClient redissonClient = wrapperRedisClient(properties);
        log.info("动态线程池，注册器（redis）链接初始化完成。{} {} {}", properties.getHost(), properties.getPoolSize(), !redissonClient.isShutdown());
        return redissonClient;
    }

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

    @Bean
    public Registry redisRegistry(RedissonClient redissonClient) {
        return new RedisRegistry(redissonClient);
    }

    @Bean
    public ThreadPoolDataReportJob threadPoolDataReportJob(DynamicThreadPoolService dynamicThreadPoolService, Registry registry) {
        return new ThreadPoolDataReportJob(dynamicThreadPoolService, registry);
    }

    @Bean
    public ThreadPoolConfigAdjustListener threadPoolConfigAdjustListener(DynamicThreadPoolService dynamicThreadPoolService, Registry registry) {
        return new ThreadPoolConfigAdjustListener(dynamicThreadPoolService, registry);
    }

    @Bean(name = "dynamicThreadPoolRedisTopic")
    public RTopic threadPoolConfigAdjustListener(ApplicationContext applicationContext, RedissonClient redissonClient, ThreadPoolConfigAdjustListener threadPoolConfigAdjustListener) {
        RTopic topic = redissonClient.getTopic(RegistryEnumVO.DYNAMIC_THREAD_POOL_REDIS_TOPIC.getKey() + "_" + getApplicationName(applicationContext));
        topic.addListener(ThreadPoolConfig.class, threadPoolConfigAdjustListener);
        return topic;
    }


    private String getApplicationName(ApplicationContext applicationContext) {
        return applicationContext.getEnvironment().getProperty("spring.application.name");
    }

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
