package com.lc.task.reflex;

import com.lc.task.common.mysql.model.DistributionTaskInvoke;
import com.lc.task.service.TaskService;

/**
 * @author liucheng
 * @create 2018-05-16 10:05
 **/
public class BeanInvoke {

    public static DistributionTaskInvoke invoke(String path, Object... objects) throws Exception {
        TaskService service = (TaskService) Reflection.newInstance(path);
        return service.execute(objects);
    }

    public static DistributionTaskInvoke invoke(Class clz, Object... objects) throws Exception {
        TaskService service = (TaskService) Reflection.newInstance(clz);
        return service.execute(objects);
    }

    public static DistributionTaskInvoke invokeBack(Class clz, Object... objects) throws Exception {
        TaskService service = (TaskService) Reflection.newInstance(clz);
        return service.executeBack(objects);
    }

}
