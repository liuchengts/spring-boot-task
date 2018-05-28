package com.lc.task.common;

import com.lc.task.config.TaskConfig;
import com.lc.task.processor.ListenEvent;
import com.lc.task.processor.SendEvent;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

/**
 * 配置入口
 *
 * @author liucheng
 * @create 2018-05-11 18:30
 **/
@Configuration
@ComponentScan({"com.lc"})
@EntityScan({"com.lc.task.common"})
@EnableRedisRepositories("com.lc.task.common.redis.*")
@EnableJpaRepositories("com.lc.task.common.mysql.*")
public class TaskAutoConfiguration {

    @Bean
    public TaskConfig taskConfig() {
        return new TaskConfig();
    }

    @Bean
    public SendEvent sendEvent() {
        return new SendEvent();
    }

    @Bean
    public ListenEvent listenEvent() {
        return new ListenEvent();
    }
}
