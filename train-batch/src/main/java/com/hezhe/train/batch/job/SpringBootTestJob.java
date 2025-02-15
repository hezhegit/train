package com.hezhe.train.batch.job;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 *  适合单体应用 不适合集群
 *  集群的每个单体都会调用，造成资源浪费
 *  可通过增加分布式锁，确保集群中只有一个在执行 =》解决办法
 *  没法实时更改定时任务状态和策略=》没法解决
 */
@Component
@EnableScheduling
public class SpringBootTestJob {

    // 表示秒数，秒数/5 余数是0，==0， 5， 10...触发
    @Scheduled(cron = "0/5 * * * * ?")
    private void test() {
        System.out.println("SpringBootTestJob test");
    }

}
