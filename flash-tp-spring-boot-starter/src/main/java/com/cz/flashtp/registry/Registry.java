package com.cz.flashtp.registry;

import com.cz.flashtp.domain.entity.ThreadPoolConfig;

import java.util.List;

/**
 * 注册中心接口
 * 注册表接口，用于报告线程池配置信息。
 * 该接口定义了两种报告机制：一种是批量报告线程池配置信息，另一种是报告单个线程池的配置参数。
 *
 * @author Zjianru
 */
public interface Registry {

    /**
     * 批量报告线程池配置信息。
     * 此方法用于一次性向注册表报告多个线程池的配置信息。这在系统启动时或配置动态更新时特别有用。
     *
     * @param threadPools 线程池配置列表，包含多个线程池的配置信息。
     *                    每个线程池的配置信息由ThreadPoolConfig对象表示。
     */
    void reportThreadPool(List<ThreadPoolConfig> threadPools);

    /**
     * 报告单个线程池的配置参数。
     * 此方法用于向注册表报告单个线程池的配置参数。这可能在线程池配置动态变化时被调用，以更新注册表中的信息。
     *
     * @param threadPoolConfig 单个线程池的配置信息。
     *                         ThreadPoolConfig对象包含线程池的所有配置参数，如核心线程数、最大线程数等。
     */
    void reportThreadPoolConfigParameter(ThreadPoolConfig threadPoolConfig);
}
