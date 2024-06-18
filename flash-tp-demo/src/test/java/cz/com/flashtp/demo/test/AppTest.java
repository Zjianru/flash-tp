package cz.com.flashtp.demo.test;

import com.cz.flashtp.domain.entity.ThreadPoolConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RTopic;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;


@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class AppTest {

    @Resource
    private RTopic dynamicThreadPoolRedisTopic;

    @Test
    public void test_dynamicThreadPoolRedisTopic() throws InterruptedException {
        ThreadPoolConfig config = ThreadPoolConfig.getInstance("dynamic-thread-pool-test-app", "threadPoolExecutor01");
        config.setPoolSize(100);
        config.setMaximumPoolSize(100);
        dynamicThreadPoolRedisTopic.publish(config);
        new CountDownLatch(1).await();
    }
}