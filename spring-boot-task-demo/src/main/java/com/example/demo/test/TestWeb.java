package com.example.demo.test;
import com.lc.task.processor.SendEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试入口
 *
 * @author liucheng
 * @create 2018-05-18 17:41
 **/
@RestController
public class TestWeb {
    @Autowired
    SendEvent sendEvent;

    @RequestMapping("/send/{expects}")
    public void send(@PathVariable("expects") String expects) {
        Map map = new HashMap();
        map.put("id", 1111);
        map.put("date", new Date());
        try {
            sendEvent.send("test", map, Collections.singleton(expects));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
