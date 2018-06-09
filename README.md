## 基于springboot的分布式事件调度任务解决方案
* 使用kafka作为事件通知
* 利用redis作为分布式协调存储
* 使用mysql存储分布式日志

## 待完成问题
* 有个脑裂情况没有考虑需要加入一个超时机制保障一下


## 处理思想
* 使用kafka的消费原则与redis特性，创造分布式任务task，及服务单元的task，做到kafka最终保障消费的最大可能性来处理任务，主要任务存储在redis中
 任务一旦完成，会移动到mysql进行持久化，并且对每步的分布式task处理有对应的操作日志存入mysql

## 使用前提：
* 需要有kafka服务、redis服务、mysql服务

##  1、需要在你的项目中写一个启动器，用来配置你的自定义任务主题与对应的任务处理实例 以下是一个例子：

@Component
@Order 
public class TransactionHandleRegister extends TransactionHandleRegisterImpl implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Map<String, Class> handleRegisterMap = new HashMap<>();
        //增加一个参与者  test，参与操作实例 Test.class
        handleRegisterMap.put("test", Test.class);
        super.init(handleRegisterMap);
    }
}

##  2、需要在你的项目中写一个任务处理器来处理实际的任务 以下是一个例子：

public class Test extends TransactionService {

    @Override
    public DistributionTaskInvoke exec(Object... objects) throws Exception {
        DistributionTaskInvoke rlt = new DistributionTaskInvoke();
        //这里写你的执行代码
        System.out.println("test exec");
        return rlt;
    }
    
    @Override
        public DistributionTaskInvoke execBack(Map map) throws Exception {
            DistributionTaskInvoke rlt = new DistributionTaskInvoke();
            //这里写你的执行失败后回滚代码
            System.out.println("test execBack==" + map.toString());
            return rlt;
        }
}
##  3、使用示例
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