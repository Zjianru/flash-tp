package com.cz.flashtp.domain;

import com.cz.flashtp.domain.entity.ThreadPoolConfig;

import java.util.List;

/**
 * 动态线程池服务
 *
 * @author Zjianru
 */
public interface DynamicThreadPoolService {

    List<ThreadPoolConfig> queryThreadPoolList();

    ThreadPoolConfig queryThreadPoolConfigByName(String threadPoolName);

    void updateThreadPoolConfig(ThreadPoolConfig threadPoolConfig);
}
