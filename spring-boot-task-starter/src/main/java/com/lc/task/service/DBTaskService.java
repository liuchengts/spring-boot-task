package com.lc.task.service;

import com.lc.task.common.mysql.model.DBTransactionTask;
import com.lc.task.common.mysql.model.DistributionTaskInvoke;
import com.lc.task.common.redis.model.TransactionTask;

import java.util.List;

/**
 * db操作的业务logs
 *
 * @author liucheng
 * @create 2018-05-22 08:37
 **/
public interface DBTaskService {
    /**
     * 保存任务
     *
     * @param transactionTask 任务
     */
    void seveTransactionTask(TransactionTask transactionTask) throws Exception;

    /**
     * 修改任务状态
     *
     * @param taskId 任务id
     * @param status 状态
     */
    void updateTransactionTask(Long taskId, Integer status);

    /**
     * 更新任务记录
     * @param dbTransactionTask 任务记录
     */
    void updateDBTransactionTask(DBTransactionTask dbTransactionTask);

    /**
     * 根据id获得一个任务
     *
     * @param taskId id
     * @return 任务对象  不存在等于null
     */
    DBTransactionTask getDBTransactionTask(Long taskId);

    /**
     * 任务异常保存
     *
     * @param taskId    任务id
     * @param errorText 异常消息
     */
    void updateTransactionTaskError(Long taskId, String errorText);

    /**
     * 保存子任务执行结果
     *
     * @param distributionTaskInvoke 子任务执行结果
     */
    void seveDistributionTaskInvoke(DistributionTaskInvoke distributionTaskInvoke);

    /**
     * 根据taskId和参与者名称模糊查询
     *
     * @param taskId    任务id
     * @param possessor 参与者
     * @return 返回锁列表
     */
    List<DistributionTaskInvoke> findSucceedByTaskIdAndPossessorLike(Long taskId, String possessor);
}
