package com.lc.task.service;

import java.util.Map;

/**
 * @author liucheng
 * @create 2018-05-11 18:15
 **/
public abstract class TaskHandleRegisterImpl implements ConfigService {
    Map<String, Class> handleRegisterMap;

    public void init(Map<String, Class> handleRegisterMap) {
        this.handleRegisterMap = handleRegisterMap;
    }

    /**
     * 获得一个处理class
     *
     * @param key key
     * @return
     */
    public Class getClass(String key) {
        if (handleRegisterMap.containsKey(key)) {
            return handleRegisterMap.get(key);
        }
        return null;
    }

}
