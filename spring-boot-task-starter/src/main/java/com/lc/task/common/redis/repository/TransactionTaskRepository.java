package com.lc.task.common.redis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.lc.task.common.redis.model.TransactionTask;
import java.io.Serializable;

/**
 * @author liucheng
 * @create 2018-05-11 18:12
 **/
@Repository
public interface TransactionTaskRepository extends JpaRepository<TransactionTask, Serializable> {
}
