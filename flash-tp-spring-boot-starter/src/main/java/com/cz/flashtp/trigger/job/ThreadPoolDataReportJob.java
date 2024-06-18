package com.cz.flashtp.trigger.job;

import com.alibaba.fastjson2.JSON;
import com.cz.flashtp.domain.DynamicThreadPoolService;
import com.cz.flashtp.domain.entity.ThreadPoolConfig;
import com.cz.flashtp.registry.Registry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

/**
 * 线程池数据上报任务
 * 动态线程池数据报告任务类。
 * 该类负责定时获取动态线程池的配置信息，并将这些信息上报到注册中心。
 *
 * @author Zjianru
 */
@Slf4j
public class ThreadPoolDataReportJob {

    /**
     * 动态线程池服务，用于获取线程池配置信息。
     */
    private final DynamicThreadPoolService dynamicThreadPoolService;

    /**
     * 注册中心，用于上报线程池配置信息。
     */
    private final Registry registry;

    /**
     * 构造函数，初始化动态线程池服务和注册中心。
     *
     * @param dynamicThreadPoolService 动态线程池服务
     * @param registry                 注册中心
     */
    public ThreadPoolDataReportJob(DynamicThreadPoolService dynamicThreadPoolService, Registry registry) {
        this.dynamicThreadPoolService = dynamicThreadPoolService;
        this.registry = registry;
    }

    /**
     * 定时任务，每20秒执行一次，用于上报线程池的配置信息。
     * 该方法首先从动态线程池服务中获取线程池配置列表，然后将这些配置信息上报到注册中心。
     * 最后，逐个上报每个线程池的详细配置参数。
     */
//    @Scheduled(cron = "0/20 * * * * ?")
    @Scheduled(cron = "0/2 * * * * ?")
    public void execReportThreadPoolList() {
        // 从动态线程池服务中查询线程池列表
        List<ThreadPoolConfig> configs = dynamicThreadPoolService.queryThreadPoolList();
        // 上报线程池配置信息
        registry.reportThreadPool(configs);
        // 日志记录上报的线程池信息
        log.info("[flash-tp] ==> 动态线程池，上报线程池信息：{}", JSON.toJSONString(configs));
        // 遍历线程池配置列表，逐个上报每个线程池的详细配置参数
        for (ThreadPoolConfig config : configs) {
            registry.reportThreadPoolConfigParameter(config);
            // 日志记录上报的线程池详细配置参数
            log.info("[flash-tp] ==> 动态线程池，上报线程池配置：{}", JSON.toJSONString(config));
        }
    }

}

