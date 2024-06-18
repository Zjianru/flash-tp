package com.cz.flashtp.config;

import com.cz.flashtp.domain.DynamicThreadPoolService;
import com.cz.flashtp.domain.entity.ThreadPoolConfig;
import com.cz.flashtp.domain.invoker.DefaultThreadPoolService;
import com.cz.flashtp.domain.valobj.RegistryEnumVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * code desc
 *
 * @author Zjianru
 */
@Configuration
@Slf4j
public class FlashTpAutoConfig {

    @Bean("flashTpService")
    public DynamicThreadPoolService flashTpService(ApplicationContext applicationContext, Map<String, ThreadPoolExecutor> threadPoolExecutors) {
        String applicationName = applicationContext.getEnvironment().getProperty("spring.application.name");
        if (StringUtils.isBlank(applicationName)) {
            applicationName = "flash-tp-defaultApp";
            log.warn("[flash-tp]==> applicationName is null and will given after process default application name");
        }
        log.info("current get thread pool info is {}", threadPoolExecutors.keySet());

        // 获取缓存数据，设置本地线程池配置
//        Set<String> threadPoolKeys = threadPoolExecutors.keySet();
//        for (String threadPoolKey : threadPoolKeys) {
//            ThreadPoolConfig threadPoolConfigEntity = redissonClient
//                    .<ThreadPoolConfig>getBucket(
//                            RegistryEnumVO.THREAD_POOL_CONFIG_PARAMETER_LIST_KEY.getKey()
//                                    + "_" + applicationName
//                                    + "_" + threadPoolKey)
//                    .get();
//            if (null == threadPoolConfigEntity) continue;
//            ThreadPoolExecutor threadPoolExecutor = threadPoolExecutors.get(threadPoolKey);
//            threadPoolExecutor.setCorePoolSize(threadPoolConfigEntity.getCorePoolSize());
//            threadPoolExecutor.setMaximumPoolSize(threadPoolConfigEntity.getMaximumPoolSize());
//        }
        return new DefaultThreadPoolService(applicationName, threadPoolExecutors) ;
    }

}
