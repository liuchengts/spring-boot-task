package com.lc.task.service;

import com.lc.task.common.mysql.model.DistributionTaskInvoke;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liucheng
 * @create 2018-05-15 20:53
 **/
public abstract class TaskService {

    public TaskService() {
    }

    public DistributionTaskInvoke execute(Object... objects) {
        DistributionTaskInvoke rlt;
        try {
            System.out.println("TransactionService exec");
            rlt = this.exec(initData(objects));
            rlt.setSucceed(true);
        } catch (Exception e) {
            rlt = new DistributionTaskInvoke();
            rlt.setSucceed(false);
            rlt.setText(e.getStackTrace().toString());
        }
        return rlt;
    }

    public DistributionTaskInvoke executeBack(Object... objects) {
        DistributionTaskInvoke rlt;
        try {
            System.out.println("TransactionService execBack");
            rlt = this.execBack(initData(objects));
            rlt.setSucceed(true);
        } catch (Exception e) {
            rlt = new DistributionTaskInvoke();
            rlt.setSucceed(false);
            rlt.setText(e.getStackTrace().toString());
        }
        return rlt;
    }

    /**
     * 初始化参数
     *
     * @param objects 参数
     * @return 返回map结构
     */
    private Map initData(Object... objects) {
        Map map = new HashMap();
        if (objects != null && objects.length > 0) {
            //默认objects第一个参数是map
            map = (Map) objects[0];
        }
        return map;
    }

    public abstract DistributionTaskInvoke exec(Map map) throws Exception;

    public abstract DistributionTaskInvoke execBack(Map map) throws Exception;
}
