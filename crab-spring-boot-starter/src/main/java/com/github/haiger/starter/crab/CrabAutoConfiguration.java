package com.github.haiger.starter.crab;

import com.github.haiger.crab.LeaderElect;
import com.github.haiger.crab.LockContext;
import com.github.haiger.crab.ReentrantLock;
import com.github.haiger.crab.conf.Config;
import com.github.haiger.crab.conf.LockEngine;
import com.github.haiger.crab.conf.RedisConfig;
import com.github.haiger.crab.util.CrabException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Haiger
 * @version $Id: CrabAutoConfiguration.java, v 0.1 2018-01-12 17:00:44 Haiger Exp $
 */
@Configuration
@ConditionalOnClass({LockContext.class, ReentrantLock.class, LeaderElect.class})
@EnableConfigurationProperties(CrabProperties.class)
public class CrabAutoConfiguration {
    @Autowired
    private CrabProperties props;

    @Bean(name = "reentrantLock")
    public ReentrantLock reentrantLock(LockContext lockContext) throws CrabException {
        ReentrantLock crabReentrantLock = new ReentrantLock(lockContext);
        return crabReentrantLock;
    }

    @Bean(name = "leaderElect")
    public LeaderElect leaderElect(LockContext lockContext) throws CrabException {
        if (props.isLeaderEnable()) {
            return new LeaderElect(lockContext);
        }
        return null;
    }

    @Bean(name = "lockContext")
    public LockContext lockContext() throws CrabException {
        Config config = Config.getInstance();
        config.setEngine(props.getLockEngine());
        config.setSpinIntervalTime(props.getLock().getSpinIntervalTime());
        config.setLeaderName(props.getLeaderName());
        config.setNamePrefix(props.getLock().getNamePrefix());
        config.setNameSuffix(props.getLock().getNameSuffix());
        config.setLeaseTime(props.getLock().getLeaseTime());
        config.setRenewIntervalTime(props.getLock().getRenewIntervalTime());
        config.setRenewExecutorCoreSize(props.getLock().getRenewExecutorCoreSize());
        config.setRenewExecutorMaxSize(props.getLock().getRenewExecutorMaxSize());
        config.setRenewExecutorQueueSize(props.getLock().getRenewExecutorQueueSize());

        LockEngine lockEngine = LockEngine.valueOf(config.getEngine());
        if (lockEngine == LockEngine.REDIS) {
            RedisConfig redisConfig = new RedisConfig();
            redisConfig.setDatabase(props.getLock().getRedisEngine().getDatabase());
            redisConfig.setPassword(props.getLock().getRedisEngine().getPassword());
            redisConfig.setNodes(props.getLock().getRedisEngine().getNodes());
            redisConfig.setConnectionTimeout(props.getLock().getRedisEngine().getConnectionTimeout());
            redisConfig.setReadTimeout(props.getLock().getRedisEngine().getReadTimeout());
            redisConfig.setPoolMaxActive(props.getLock().getRedisEngine().getPool().getMaxActive());
            redisConfig.setPoolMaxIdle(props.getLock().getRedisEngine().getPool().getMaxIdle());
            redisConfig.setPoolMinIdle(props.getLock().getRedisEngine().getPool().getMinIdle());
            redisConfig.setPoolMaxWait(props.getLock().getRedisEngine().getPool().getMaxWait());
            redisConfig.setPoolTestOnBorrow(props.getLock().getRedisEngine().getPool().isTestOnBorrow());

            config.setRedisConfig(redisConfig);
        }

        return LockContext.getInstance().build(config);
    }
}
