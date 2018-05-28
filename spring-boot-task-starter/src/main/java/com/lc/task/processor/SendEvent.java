package com.lc.task.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lc.task.common.mysql.model.DBTransactionTask;
import com.lc.task.common.redis.model.TransactionTask;
import com.lc.task.common.redis.repository.TransactionTaskRepository;
import com.lc.task.common.Topcs;
import com.lc.task.service.DBTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * 发送事件
 *
 * @author liucheng
 * @create 2018-05-11 17:57
 **/
@Slf4j
@Component
@EnableAsync
public class SendEvent {
    @Autowired
    KafkaTemplate kafkaTemplate;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    TransactionTaskRepository transactionTaskRepository;
    @Autowired
    ServerConfig serverConfig;
    @Autowired
    DBTaskService dBTaskService;


    /**
     * 发送消息
     *
     * @param top     top发送地址
     * @param content 发送内容
     */
    @Async
    public void send(String top, String content) {
        kafkaTemplate.send(top, content);
    }

    /**
     * 发送事务消息
     *
     * @param theme      要处理的事件主题
     * @param parameters 执行参数
     * @param expects    要参与的人名称
     */
    @Async
    public void send(String theme, Map parameters, Set<String> expects) throws Exception {
        TransactionTask transactionTask = null;
        String parametersJson = null;
        if (parameters != null && !parameters.isEmpty()) {
            parametersJson = objectMapper.writeValueAsString(parameters);
        }
        try {
            transactionTask = transactionTaskRepository.save(TransactionTask.builder()
                    .sponsor(serverConfig.getSossessor())
                    .theme(theme)
                    .parameters(parametersJson)
                    .expects(expects)
                    .expectNum(expects.size())
                    .build());
            dBTaskService.seveTransactionTask(transactionTask);
            send(Topcs.TASK, transactionTask.getId().toString());
            dBTaskService.updateTransactionTask(transactionTask.getId(), DBTransactionTask.StatusEnum.WAIT_CHECK.key);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("kafka消息发送失败：theme=" + theme + " |parameters=" + (parameters == null ? "" : parameters.toString()) +
                    " |expects=" + expects.toString(), e);
            dBTaskService.updateTransactionTaskError(transactionTask.getId(), e.getStackTrace().toString());
        }
    }


}