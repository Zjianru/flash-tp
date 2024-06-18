package com.cz.flashtp.domain.invoker;

import com.alibaba.fastjson2.JSON;
import com.cz.flashtp.domain.DynamicThreadPoolService;
import com.cz.flashtp.domain.entity.ThreadPoolConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 默认的线程池服务
 *
 * @author Zjianru
 */
@Slf4j
public class DefaultThreadPoolService implements DynamicThreadPoolService {

    /**
     * 默认的线程池服务类，用于管理应用程序的线程池配置和执行器。
     * 通过提供线程池的查询和配置更新功能，支持动态调整线程池参数。
     */
    private final String applicationName;
    private final Map<String, ThreadPoolExecutor> threadPoolExecutorMap;

    /**
     * 构造函数初始化DefaultThreadPoolService。
     *
     * @param applicationName       应用程序名称，用于标识线程池所属的应用。
     * @param threadPoolExecutorMap 线程池执行器的映射，键为线程池名称，值为ThreadPoolExecutor实例。
     */
    public DefaultThreadPoolService(String applicationName, Map<String, ThreadPoolExecutor> threadPoolExecutorMap) {
        this.applicationName = applicationName;
        this.threadPoolExecutorMap = threadPoolExecutorMap;
    }

    /**
     * 查询所有线程池的配置信息。
     *
     * @return 包含所有线程池配置的列表。
     */
    @Override
    public List<ThreadPoolConfig> queryThreadPoolList() {
        // 获取线程池名称的集合
        Set<String> threadPoolBeanNames = threadPoolExecutorMap.keySet();
        List<ThreadPoolConfig> threadPools = new ArrayList<>(threadPoolBeanNames.size());
        // 遍历每个线程池，获取其配置信息
        for (String beanName : threadPoolBeanNames) {
            ThreadPoolExecutor threadPoolExecutor = threadPoolExecutorMap.get(beanName);
            ThreadPoolConfig config = ThreadPoolConfig.getInstance(applicationName, beanName, threadPoolExecutor);
            // 日志记录线程池配置信息
            log.debug("[flash-tp]==>queryThreadPoolList==>动态线程池，配置查询 应用名:{} 线程名:{} 池化配置:{}", applicationName, beanName, JSON.toJSONString(config));
            threadPools.add(config);
        }
        return threadPools;
    }

    /**
     * 根据线程池名称查询其配置信息。
     *
     * @param threadPoolName 线程池的名称。
     * @return 对应线程池的配置信息。
     */
    @Override
    public ThreadPoolConfig queryThreadPoolConfigByName(String threadPoolName) {
        ThreadPoolExecutor threadPoolExecutor = threadPoolExecutorMap.get(threadPoolName);
        if (null == threadPoolExecutor) return ThreadPoolConfig.getInstance(applicationName, threadPoolName);
        // 线程池配置数据
        ThreadPoolConfig config = ThreadPoolConfig.getInstance(applicationName, threadPoolName, threadPoolExecutor);
        // 日志记录线程池配置信息
        log.debug("[flash-tp]==>queryThreadPoolConfigByName==>动态线程池，配置查询 应用名:{} 线程名:{} 池化配置:{}", applicationName, threadPoolName, JSON.toJSONString(config));
        return config;
    }

    /**
     * 更新线程池的配置。
     *
     * @param threadPoolConfig 需要更新的线程池配置信息。
     */
    @Override
    public void updateThreadPoolConfig(ThreadPoolConfig threadPoolConfig) {
        if (null == threadPoolConfig || !applicationName.equals(threadPoolConfig.getAppName())) return;
        ThreadPoolExecutor threadPoolExecutor = threadPoolExecutorMap.get(threadPoolConfig.getThreadPoolName());
        if (null == threadPoolExecutor) return;
        // 设置参数 「调整核心线程数和最大线程数」
        threadPoolExecutor.setCorePoolSize(threadPoolConfig.getCorePoolSize());
        threadPoolExecutor.setMaximumPoolSize(threadPoolConfig.getMaximumPoolSize());
    }

}
