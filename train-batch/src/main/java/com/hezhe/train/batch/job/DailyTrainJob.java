package com.hezhe.train.batch.job;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.hezhe.train.batch.feign.BusinessFeign;
import com.hezhe.train.common.resp.Result;
import jakarta.annotation.Resource;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Date;

// DisallowConcurrentExecution 禁止并发执行
@DisallowConcurrentExecution
public class DailyTrainJob implements Job {

    private static final Logger LOG = LoggerFactory.getLogger(DailyTrainJob.class);

    @Resource
    private BusinessFeign businessFeign;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        // 增加日志流水号
        MDC.put("LOG_ID", System.currentTimeMillis() + RandomUtil.randomString(3));
        LOG.info("===生成15天后的车次数据开始===");

        Date now = new Date();
        DateTime dateTime = DateUtil.offsetDay(now, 15);
        Date date = dateTime.toJdkDate();
        Result result = businessFeign.genDaily(date);

        LOG.info("===结果：{} ===", result);
        LOG.info("===生成15天后的车次数据结束===");

    }
}
