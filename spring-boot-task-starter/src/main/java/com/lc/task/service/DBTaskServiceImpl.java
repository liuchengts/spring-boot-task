package com.lc.task.service;

import com.lc.task.common.mysql.model.DBTransactionTask;
import com.lc.task.common.mysql.model.DistributionTaskInvoke;
import com.lc.task.common.mysql.repository.DBTransactionTaskRepository;
import com.lc.task.common.mysql.repository.DistributionTaskInvokeRepository;
import com.lc.task.common.redis.model.TransactionTask;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * db实现
 *
 * @author liucheng
 * @create 2018-05-22 08:41
 **/
@Service
public class DBTaskServiceImpl implements DBTaskService {

    @Autowired
    DistributionTaskInvokeRepository distributionTaskInvokeRepository;
    @Autowired
    DBTransactionTaskRepository dBTransactionTaskRepository;


    @Override
    public void seveTransactionTask(TransactionTask transactionTask) throws Exception {
        DBTransactionTask dbTransactionTask = new DBTransactionTask();
        BeanUtils.copyProperties(transactionTask, dbTransactionTask);
        dbTransactionTask.setExpects(transactionTask.getExpects().toString());
        dbTransactionTask.setUpdateAt(new Date());
        dbTransactionTask.setStatus(DBTransactionTask.StatusEnum.NOT_STARTED.key);
        dBTransactionTaskRepository.save(dbTransactionTask);
    }

    @Override
    public void updateTransactionTask(Long taskId, Integer status) {
        DBTransactionTask dbTransactionTask = getDBTransactionTask(taskId);
        if (dbTransactionTask != null && (dbTransactionTask.getStatus().equals(DBTransactionTask.StatusEnum.CHECK_SUCCEED.key))) {
            dbTransactionTask.setStatus(status);
            dBTransactionTaskRepository.save(dbTransactionTask);
        }
    }

    @Override
    public void updateDBTransactionTask(DBTransactionTask dbTransactionTask) {
        if (dbTransactionTask == null || dbTransactionTask.getId() == null) {
            return;
        }
        dBTransactionTaskRepository.save(dbTransactionTask);
    }

    @Override
    public DBTransactionTask getDBTransactionTask(Long taskId) {
        Optional<DBTransactionTask> optional = dBTransactionTaskRepository.findById(taskId);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    @Override
    public void updateTransactionTaskError(Long taskId, String errorText) {
        DBTransactionTask dbTransactionTask = getDBTransactionTask(taskId);
        if (dbTransactionTask != null && (dbTransactionTask.getStatus().equals(DBTransactionTask.StatusEnum.CHECK_SUCCEED.key))) {
            dbTransactionTask.setStatus(DBTransactionTask.StatusEnum.FAILURE.key);
            dbTransactionTask.setErrorText(errorText);
            dBTransactionTaskRepository.save(dbTransactionTask);
        }

    }

    @Override
    public void seveDistributionTaskInvoke(DistributionTaskInvoke distributionTaskInvoke) {
        distributionTaskInvokeRepository.save(distributionTaskInvoke);
    }

    @Override
    public List<DistributionTaskInvoke> findSucceedByTaskIdAndPossessorLike(Long taskId, String possessor) {
        return distributionTaskInvokeRepository.findByTaskIdAndSucceedTrueAndPossessorLike(taskId, possessor);
    }
}
