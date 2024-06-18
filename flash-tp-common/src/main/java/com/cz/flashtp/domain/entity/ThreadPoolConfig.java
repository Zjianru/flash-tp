package com.cz.flashtp.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置
 *
 * @author Zjianru
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThreadPoolConfig {
    /**
     * 应用名称
     */
    private String appName;

    /**
     * 线程池名称
     */
    private String threadPoolName;

    /**
     * 核心线程数
     */
    private int corePoolSize;

    /**
     * 最大线程数
     */
    private int maximumPoolSize;

    /**
     * 当前活跃线程数
     */
    private int activeCount;

    /**
     * 当前池中线程数
     */
    private int poolSize;

    /**
     * 队列类型
     */
    private String queueType;

    /**
     * 当前队列任务数
     */
    private int queueSize;

    /**
     * 队列剩余任务数
     */
    private int remainingCapacity;

    public static ThreadPoolConfig getInstance(String appName, String threadPoolName, ThreadPoolExecutor threadPool) {
        return  ThreadPoolConfig.builder()
                .appName(appName)
                .threadPoolName(threadPoolName)
                .corePoolSize(threadPool.getCorePoolSize())
                .maximumPoolSize(threadPool.getMaximumPoolSize())
                .activeCount(threadPool.getActiveCount())
                .poolSize(threadPool.getPoolSize())
                .queueType(threadPool.getQueue().getClass().getSimpleName())
                .queueSize(threadPool.getQueue().size())
                .remainingCapacity(threadPool.getQueue().remainingCapacity())
                .build();
    }

    public static ThreadPoolConfig getInstance(String applicationName, String threadPoolName) {
        return ThreadPoolConfig.builder()
                .appName(applicationName)
                .threadPoolName(threadPoolName)
                .build();
    }
}
