package com.lc.task.common.redis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.util.Date;

/**
 * 参与者获得的数据锁
 *
 * @author liucheng
 * @create 2018-05-15 17:18
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("distribution_task")
public class DistributionTask implements Serializable {
    @Id
    private Long id;
    /**
     * 所有者
     */
    @Indexed
    private String possessor;
    /**
     * 任务id
     */
    @Indexed
    private Long taskId;
    /**
     * 是否已完成
     */
    @Builder.Default
    private Boolean isComplete = Boolean.FALSE;

    /**
     * 创建时间
     */
    private Date createAt;

}
