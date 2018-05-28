package com.example.demo;

import com.lc.task.common.mysql.model.DBTransactionTask;
import com.lc.task.common.redis.model.DistributionTask;
import com.lc.task.common.redis.repository.DistributionTaskRepository;
import com.lc.task.common.redis.repository.TransactionTaskRepository;
import com.lc.task.service.DBTaskService;
import com.mysql.jdbc.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = DemoApplication.class)
public class DemoApplicationTests {
    @Autowired
    TransactionTaskRepository transactionTaskRepository;
    @Autowired
    DistributionTaskRepository distributionTaskRepository;
    @Autowired
    DBTaskService dBTaskService;

    Long taskId = -4672765884838793092L;

    @Test
    public void jpaHibernateTest() {
        DBTransactionTask dbTransactionTask = dBTaskService.getDBTransactionTask(taskId);
        System.out.println(dbTransactionTask == null);
        System.out.println(dbTransactionTask.getSponsor());
    }

    @Test
    public void jpaRedisTest() {
        String sossessor = "transaction-demo8080";
        List<DistributionTask> list1 = distributionTaskRepository.findByTaskIdAndPossessorOrderByIdAsc(864362991637115552l, sossessor);
        List<DistributionTask> list2 = distributionTaskRepository.findByTaskId(864362991637115552l);
        distributionTaskRepository.deleteAll(list2);
        list2 = distributionTaskRepository.findByTaskId(864362991637115552l);
        System.out.println(list2 == null);
        //        transactionTaskRepository.deleteById(taskId);
    }

    @Test
    public void setTest() {
        Set<String> practicalExpects = new HashSet<>();
        practicalExpects.add("张三");
        practicalExpects.add("李四");
        String str = practicalExpects.toString();
        System.out.println(str);
        Set<String> setlist = stringToSet(str);
        for (String s : setlist) {
            System.out.println("s:" + s);
        }

    }

    /**
     * 将字符串转换成Set
     *
     * @param str
     * @return
     */
    private Set<String> stringToSet(String str) {
        if (StringUtils.isNullOrEmpty(str) || str.length() < 2) {
            return new HashSet<>();
        }
        str = str.substring(1, str.length() - 1);
        return new HashSet(Arrays.asList(str.split(",")));
    }
}
