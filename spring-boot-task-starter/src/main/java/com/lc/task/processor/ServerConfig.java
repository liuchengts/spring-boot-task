package com.lc.task.processor;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 当前server的配置
 * @author liucheng
 * @create 2018-05-21 20:47
 **/
@Component
@Scope
public class ServerConfig {
    @Getter
    @Value("${spring.application.name}")
    private String sponsor;
    @Getter
    @Value("${server.port}")
    private String port;

    /**
     * 获得服务器唯一标识
     *
     * @return
     */
    public String getSossessor() {
        return sponsor + port;
    }
}
