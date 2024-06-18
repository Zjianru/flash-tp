package com.cz.flashtp.trigger.listener;

import com.alibaba.fastjson2.JSON;
import com.cz.flashtp.domain.DynamicThreadPoolService;
import com.cz.flashtp.domain.entity.ThreadPoolConfig;
import com.cz.flashtp.registry.Registry;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.listener.MessageListener;

import java.util.List;

/**
 * 动态线程池变更监听
 * 实现了MessageListener接口，用于监听线程池配置的调整消息。
 * 当收到线程池配置调整的消息时，会动态更新线程池配置，并上报更新后的配置给注册中心。
 *
 * @author Zjianru
 */
@Slf4j
public class ThreadPoolConfigAdjustListener implements MessageListener<ThreadPoolConfig> {

    /**
     * 动态线程池服务，用于更新和查询线程池配置。
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
    public ThreadPoolConfigAdjustListener(DynamicThreadPoolService dynamicThreadPoolService, Registry registry) {
        this.dynamicThreadPoolService = dynamicThreadPoolService;
        this.registry = registry;
    }

    /**
     * 当收到线程池配置调整的消息时，执行此方法。
     * 先日志记录配置信息，然后更新线程池配置，最后上报更新后的线程池配置给注册中心。
     *
     * @param charSequence 消息内容，这里未使用
     * @param needChange   需要调整的线程池配置
     */
    @Override
    public void onMessage(CharSequence charSequence, ThreadPoolConfig needChange) {
        // 日志记录线程池配置调整信息
        log.info("动态线程池，调整线程池配置。线程池名称:{} 核心线程数:{} 最大线程数:{}", needChange.getThreadPoolName(), needChange.getPoolSize(), needChange.getMaximumPoolSize());

        // 更新线程池配置
        dynamicThreadPoolService.updateThreadPoolConfig(needChange);

        // 上报更新后的线程池配置给注册中心
        // 更新后上报最新数据
        List<ThreadPoolConfig> storedConfig = dynamicThreadPoolService.queryThreadPoolList();
        registry.reportThreadPool(storedConfig);

        // 上报特定名称的线程池配置参数给注册中心
        ThreadPoolConfig current = dynamicThreadPoolService.queryThreadPoolConfigByName(needChange.getThreadPoolName());
        registry.reportThreadPoolConfigParameter(current);

        // 日志记录上报的线程池配置
        log.info("动态线程池，上报线程池配置：{}", JSON.toJSONString(needChange));
    }

}

