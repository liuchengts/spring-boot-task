package com.lc.task.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lc.task.common.Topcs;
import com.lc.task.common.mysql.model.DBTransactionTask;
import com.lc.task.common.mysql.model.DistributionTaskInvoke;
import com.lc.task.common.redis.model.DistributionTask;
import com.lc.task.common.redis.model.TransactionTask;
import com.lc.task.common.redis.repository.DistributionTaskRepository;
import com.lc.task.common.redis.repository.TransactionTaskRepository;
import com.lc.task.reflex.BeanInvoke;
import com.lc.task.service.ConfigService;
import com.lc.task.service.DBTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 消息监听
 *
 * @author liucheng
 * @create 2018-05-11 17:32
 **/
@Slf4j
@Component
@EnableAsync
public class ListenEvent {
    @Autowired
    TransactionTaskRepository transactionTaskRepository;
    @Autowired
    DistributionTaskRepository distributionTaskRepository;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    ConfigService configService;
    @Autowired
    ServerConfig serverConfig;
    @Autowired
    DBTaskService dBTaskService;

    @KafkaListener(topics = Topcs.TASK)
    public void handle(String msg, Acknowledgment ack) {
        log.info("收到kafka消息：" + msg);
        try {
            if (handleListener(Long.valueOf(msg))) {
                ack.acknowledge();
                log.info("kafka消息处理完成：" + msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.info("kafka消息处理失败：" + msg, e);
        }
    }


    /**
     * 消息处理器
     *
     * @param taskId 任务id
     * @return 返回是否消费掉消息
     */
    private boolean handleListener(Long taskId) throws Exception {
        //任务
        TransactionTask transactionTask;
        //当前执行的子任务
        DistributionTask lock;
        //获得任务并且根据处理机制过滤
        Object task = getHandleTask(taskId);
        if (task instanceof Boolean) {
            return (boolean) task;
        } else {
            transactionTask = (TransactionTask) task;
        }
        //执行锁策略
        Object check = checkLock(transactionTask);
        if (check instanceof Boolean) {
            return (boolean) check;
        } else {
            lock = (DistributionTask) check;
        }
        boolean fag;
        try {
            //执行事务机制
            log.info("当前任务开始执行事务 taskId:" + taskId);
            fag = invoke(transactionTask, lock, false);
        } catch (Exception e) {
            //需要进行事务回滚
            log.error("当前任务执行事务失败 taskId:" + taskId + " 进行事务回滚", e);
            invoke(transactionTask, lock, true);
            fag = false;
        }
        if (!fag) {
            log.error("当前任务执行事务失败 taskId:" + taskId);
        }
        return fag;
    }

    /**
     * 根据taskId获取要处理的任务
     *
     * @param taskId 任务id
     * @return 返回任务是否正确 / 需要执行的任务对象
     */
    private Object getHandleTask(Long taskId) throws Exception {
        Optional<TransactionTask> optional = transactionTaskRepository.findById(taskId);
        if (!optional.isPresent()) {
            log.error("当前任务id无法查询到任务，默认消费掉 taskId:" + taskId);
            return true;
        }
        TransactionTask transactionTask = optional.get();
        if (!transactionTask.getExpects().contains(serverConfig.getSponsor())) {
            log.info("当前服务不是该任务的参与者，默认消费掉 taskId:" + taskId);
            return true;
        }
        return transactionTask;
    }


    /**
     * 获得当前当前任务下当前服务组所有的锁
     *
     * @param transactionTask 主任务
     * @return 返回加锁操作是否成功 /  返回当前获得操作权限的子对象锁
     */
    private Object checkLock(TransactionTask transactionTask) throws Exception {
        //当前服务识别唯一标识
        String possessor = serverConfig.getSossessor();
        //加锁
        DistributionTask prepareLock = defaultLock(transactionTask, possessor);
        //检测锁是否生效
        List<DistributionTask> distributionList = distributionTaskRepository.findByTaskIdAndPossessorOrderByIdAsc(
                transactionTask.getId(), possessor);
        if (null == distributionList || distributionList.isEmpty()) {
            log.error("加锁失败:taskId:" + transactionTask.getId() + " sponsor:" + possessor);
            return false;
        }
        DistributionTask lock = distributionList.get(0);
        //比较生效的锁
        for (int i = 0; i < distributionList.size(); i++) {
            if (i != 0) {
                //删除锁
                distributionTaskRepository.delete(distributionList.get(i));
            }
        }
        if (!lock.getId().equals(prepareLock.getId()) && !lock.getPossessor().equals(possessor)) {
            //当前锁没生效
            log.info("当前服务没有抢占到任务锁" + lock.getId() + "，默认消费掉 taskId:" + transactionTask.getId() + " prepareLockId:" + prepareLock.getId());
            return true;
        }
        //检查当前锁是否已完成
        if (lock.getIsComplete()) {
            //当前锁已成功执行
            log.info("当前锁已成功执行" + lock.getId() + "，默认消费掉 taskId:" + transactionTask.getId());
            return true;
        }
        return lock;
    }


    /**
     * 增加默认锁
     *
     * @param transactionTask 主任务
     * @param possessor       当前服务识别唯一标识
     * @return 返回子任务对象
     */
    private DistributionTask defaultLock(TransactionTask transactionTask, String possessor) throws Exception {
        return distributionTaskRepository.save(DistributionTask.builder()
                .taskId(transactionTask.getId())
                .possessor(possessor)
                .createAt(new Date())
                .build());
    }

    /**
     * 代理执行具体的事务实例
     *
     * @param transactionTask 事务任务
     * @return 返回成功或者失败
     */
    private boolean invoke(TransactionTask transactionTask, DistributionTask lock, boolean back) throws Exception {
        DistributionTaskInvoke rlt = new DistributionTaskInvoke();
        try {
            Class clz = configService.getClass(transactionTask.getTheme());
            if (clz == null) {
                log.error("找不到处理实例 taskId:" + transactionTask.getId() + " theme:" + transactionTask.getTheme());
                rlt.setSucceed(false);
            } else {
                log.info("开始执行事务方法 " + transactionTask.toString());
                Map map = new HashMap();
                if (transactionTask.getParameters() != null) {
                    map = objectMapper.readValue(transactionTask.getParameters(), Map.class);
                }
                if (back) {
                    rlt = BeanInvoke.invokeBack(clz, map);
                } else {
                    rlt = BeanInvoke.invoke(clz, map);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            rlt.setSucceed(false);
            rlt.setText(e.getStackTrace().toString());
        } finally {
            log.info("持久化子任务结果 taskId:" + transactionTask.getId());
            addLogDistributionTaskInvoke(rlt, lock.getId(), transactionTask.getId());
        }
        if (rlt.getSucceed()) {
            checkTransactionTaskProgress(rlt, lock, transactionTask);
        }
        return rlt.getSucceed();
    }

    /**
     * 检查主任务进度
     *
     * @param rlt             子任务执行结果
     * @param transactionTask 主任务对象
     */
    private void checkTransactionTaskProgress(DistributionTaskInvoke rlt, DistributionTask lock, TransactionTask transactionTask) throws Exception {
        if (!rlt.getSucceed()) {
            return;
        }
        DBTransactionTask dbTransactionTask = dBTaskService.getDBTransactionTask(transactionTask.getId());
        if (dbTransactionTask == null) {
            return;
        }
        Set<String> expects = stringToSet(dbTransactionTask.getExpects());
        Set<String> practicalExpects = stringToSet(dbTransactionTask.getPracticalExpects());
        for (String expect : expects) {
            List<DistributionTaskInvoke> distributionList = dBTaskService.findSucceedByTaskIdAndPossessorLike(
                    transactionTask.getId(), expect + "%");
            if (distributionList != null && !distributionList.isEmpty() && !practicalExpects.contains(expect)) {
                practicalExpects.add(expect);
            }
        }
        dbTransactionTask.setPracticalExpects(practicalExpects.toString());
        dbTransactionTask.setPracticalExpectNum(practicalExpects.size());
        //更改主记录
        dBTaskService.updateDBTransactionTask(dbTransactionTask);
        if (dbTransactionTask.getPracticalExpectNum().equals(dbTransactionTask.getExpectNum())) {
            //任务完成
            delTask(transactionTask.getId());
        } else {
            //将此次执行的子任务标记为成功执行
            lock.setIsComplete(Boolean.TRUE);
            distributionTaskRepository.save(lock);
        }
    }

    /**
     * 将原先是set的结果集tostring后的字符串转换成Set
     *
     * @param str 要转换的字符串
     * @return 返回set
     */
    private Set<String> stringToSet(String str) {
        if (StringUtils.isEmpty(str) || str.length() < 2) {
            return new HashSet<>();
        }
        str = str.substring(1, str.length() - 1);
        return new HashSet(Arrays.asList(str.split(",")));
    }

    /**
     * 清除redis中的任务记录
     *
     * @param taskId 任务id
     * @throws Exception 异常
     */
    private void delTask(Long taskId) throws Exception {
        transactionTaskRepository.deleteById(taskId);
        distributionTaskRepository.deleteAll(distributionTaskRepository.findByTaskId(taskId));
    }

    /**
     * 增加子任务记录
     *
     * @param rlt                记录实体
     * @param distributionTaskId 子任务id
     * @param taskId             主任务id
     */
    private void addLogDistributionTaskInvoke(DistributionTaskInvoke rlt, Long distributionTaskId, Long taskId) {
        rlt.setDistributionTaskId(distributionTaskId);
        rlt.setTaskId(taskId);
        rlt.setPossessor(serverConfig.getSossessor());
        rlt.setCreateAt(new Date());
        dBTaskService.seveDistributionTaskInvoke(rlt);
    }
}
