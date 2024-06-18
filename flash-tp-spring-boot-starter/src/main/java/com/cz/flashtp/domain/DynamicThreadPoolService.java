package com.cz.flashtp.domain;

import com.cz.flashtp.domain.entity.ThreadPoolConfig;

import java.util.List;

/**
 * 动态线程池服务
 * 动态线程池服务接口。
 * 提供了查询线程池配置、根据名称查询线程池配置以及更新线程池配置的方法。
 *
 * @author Zjianru
 */
public interface DynamicThreadPoolService {

    /**
     * 查询所有线程池配置。
     *
     * @return 线程池配置列表。
     */
    List<ThreadPoolConfig> queryThreadPoolList();

    /**
     * 根据线程池名称查询线程池配置。
     *
     * @param threadPoolName 线程池名称。
     * @return 对应名称的线程池配置。
     */
    ThreadPoolConfig queryThreadPoolConfigByName(String threadPoolName);

    /**
     * 更新线程池配置。
     *
     * @param threadPoolConfig 新的线程池配置。
     */
    void updateThreadPoolConfig(ThreadPoolConfig threadPoolConfig);
}
