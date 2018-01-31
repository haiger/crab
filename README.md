# crab
分布式协调（锁、选举)

## Getting Started
### Artifact
```java
<dependency>
    <groupId>com.github.haiger</groupId>
    <artifactId>crab-java</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Example usage:
```java
LockContext context = LockContext.getInstance().build(Config.getInstance().load("/crab.properties"));
ReentrantLock lock = new ReentrantLock(context);

String bizName = "bizName_Test";
String bizValue = "bizValue_Test";
try {
    lock.lock(bizName, bizValue);
    sleep(1);
} finally {
    lock.unlock(bizName);
}


LeaderElect leader = new LeaderElect(context);
leader.setListener((isLeader, changeTime) -> {
            System.out.println("isLeader:" + isLeader + "---changeTime:" + changeTime);
        });
boolean isLeader = leader.isLeader();
```

## Properties Example:
```java
crab.lock.engine=REDIS
# 锁自旋的间隔
crab.lock.spin.interval-time=90
# 默认：LEADER
crab.lock.leader-name=LEADER
# 可以为项目名
crab.lock.name-prefix=CRAB_TEST
crab.lock.name-suffix=DEV
# 默认redisLock的过期时间
crab.lock.lease-time=720
# redisLock每次续租间隔
crab.lock.renew.interval-time=290
# 执行续租的线程池coreSize，默认：10
crab.lock.renew.executor.core-size=10
# 执行续租的线程池MaxSize，默认：20
crab.lock.renew.executor.max-size=20
# 执行续租的线程池queueSize，默认：1000
crab.lock.renew.executor.queue-size=1000
crab.lock.engine.redis.database=0
crab.lock.engine.redis.password=xxx
crab.lock.engine.redis.nodes=host1:6379,host2:6379,host3:6377
crab.lock.engine.redis.connection-timeout=300
crab.lock.engine.redis.read-timeout=300
crab.lock.engine.redis.pool.max-active=8
crab.lock.engine.redis.pool.max-idle=5
crab.lock.engine.redis.pool.min-idle=3
crab.lock.engine.redis.pool.max-wait=1000
crab.lock.engine.redis.pool.test-on-borrow=true

```


## Spring-boot
### Artifact
```java
<dependency>
    <groupId>com.github.haiger.starter</groupId>
    <artifactId>crab-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Spring-boot Properties Example:
```java
crab.leaderEnable=true
crab.leaderName=LEADER
crab.lockEngine=REDIS
crab.lock.spinIntervalTime=90
crab.lock.namePrefix=CRAB_TEST
crab.lock.nameSuffix=DEV
crab.lock.leaseTime=720
crab.lock.renewIntervalTime=290
crab.lock.renewExecutorCoreSize=10
crab.lock.renewExecutorMaxSize=20
crab.lock.renewExecutorQueueSize=1000
crab.lock.redisEngine.database=0
crab.lock.redisEngine.password=xxx
crab.lock.redisEngine.nodes=host1:6379,host2:6379,host3:6377
crab.lock.redisEngine.connectionTimeout=300
crab.lock.redisEngine.readTimeout=300
crab.lock.redisEngine.pool.maxActive=8
crab.lock.redisEngine.pool.maxIdle=5
crab.lock.redisEngine.pool.minIdle=3
crab.lock.redisEngine.pool.maxWait=1000
crab.lock.redisEngine.pool.testOnBorrow=true
```

### Service
```java
import com.fabric4cloud.crab.ReentrantLock;
public class SomeService {
    @Autowired
    private ReentrantLock lock;

    @Autowired
    private LeaderElect leaderElect;

    public void some() {
        String bizName = "bizName_Test";
        String bizValue = "bizValue_Test";
        try {
            if (leaderElect.isLeader()) {
                lock.lock(bizName, bizValue);
                sleep(1);
            }

        } finally {
            lock.unlock(bizName);
        }

    }

}
```