package com.example.demo.test;

import com.lc.task.common.mysql.model.DistributionTaskInvoke;
import com.lc.task.service.TaskService;

import java.util.HashMap;
import java.util.Map;

/**
 * 事务 操作实例
 *
 * @author liucheng
 * @create 2018-05-15 21:00
 **/
public class Test extends TaskService {

    @Override
    public DistributionTaskInvoke exec(Map map) throws Exception {
        DistributionTaskInvoke rlt = new DistributionTaskInvoke();
        System.out.println("test exec==" + map.toString());
        return rlt;
    }

    @Override
    public DistributionTaskInvoke execBack(Map map) throws Exception {
        DistributionTaskInvoke rlt = new DistributionTaskInvoke();
        System.out.println("test execBack==" + map.toString());
        return rlt;
    }
}
