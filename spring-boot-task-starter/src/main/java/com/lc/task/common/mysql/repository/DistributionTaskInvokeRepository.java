package com.lc.task.common.mysql.repository;

import com.lc.task.common.mysql.model.DistributionTaskInvoke;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.List;

/**
 * @author liucheng
 * @create 2018-05-22 09:02
 **/
@Repository
public interface DistributionTaskInvokeRepository extends JpaRepository<DistributionTaskInvoke, Serializable>
        , JpaSpecificationExecutor<DistributionTaskInvoke> {

    /**
     * 根据taskId和参与者名称模糊查询
     *
     * @param taskId    任务id
     * @param possessor 参与者
     * @return 返回锁列表
     */
    List<DistributionTaskInvoke> findByTaskIdAndSucceedTrueAndPossessorLike(Long taskId, String possessor);
}
