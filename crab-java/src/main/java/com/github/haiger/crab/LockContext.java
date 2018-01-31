package com.github.haiger.crab;

import com.github.haiger.crab.conf.Config;
import com.github.haiger.crab.conf.LockEngine;
import com.github.haiger.crab.engine.Lock;
import com.github.haiger.crab.engine.local.LocalLock;
import com.github.haiger.crab.engine.redis.JedisPoolFactory;
import com.github.haiger.crab.engine.redis.JedisPoolInfo;
import com.github.haiger.crab.engine.redis.RedisLock;
import com.github.haiger.crab.engine.zookeeper.ZookeeperLock;
import com.github.haiger.crab.util.CrabException;

import java.util.List;

/**
 * @author Haiger
 * @version $Id: LockContext.java, v 0.1 2018-01-11 16:03:51 Haiger Exp $
 */
public class LockContext {
    private LockEngine lockEngine;
    private Lock       currentLockEngine;

    private long   spinIntervalTime;
    private String leaderLockName;
    private String lockNamePrefix;
    private String lockNameSuffix;

    private List<JedisPoolInfo> jedisPools;

    private long lockLeaseTime;
    private long lockRenewIntervalTime;
    private int  lockRenewExecutorCoreSize;
    private int  lockRenewExecutorMaxSize;
    private int  lockRenewExecutorQueueSize;

    private LockContext() {

    }

    public static LockContext getInstance() {
        return LockContextHolder.instance;
    }

    private static class LockContextHolder {
        static final LockContext instance = new LockContext();
    }

    public LockContext build(Config config) throws CrabException {
        setLockEngine(config.getEngine());
        setSpinIntervalTime(config.getSpinIntervalTime());
        setLeaderLockName(config.getLeaderName());
        setLockNamePrefix(config.getNamePrefix());
        setLockNameSuffix(config.getNameSuffix());
        setLockLeaseTime(config.getLeaseTime());
        setLockRenewIntervalTime(config.getRenewIntervalTime());
        setLockRenewExecutorCoreSize(config.getRenewExecutorCoreSize());
        setLockRenewExecutorMaxSize(config.getRenewExecutorMaxSize());
        setLockRenewExecutorQueueSize(config.getRenewExecutorQueueSize());

        if (lockEngine == LockEngine.REDIS) {
            setJedisPools(JedisPoolFactory.createJedisPoolList(config.getRedisConfig()));
        }

        buildCurrentEngine();

        return this;
    }

    public void buildCurrentEngine() throws CrabException {
        switch (lockEngine) {
            case LOCAL:
                currentLockEngine = new LocalLock(spinIntervalTime);
                break;
            case REDIS:
                currentLockEngine = new RedisLock(jedisPools, spinIntervalTime);
                break;
            case ZOOKEEPER:
                currentLockEngine = new ZookeeperLock();
                break;
            default:
                throw new CrabException("can not support this engine:" + lockEngine.name());
        }
    }

    public Lock getCurrentLockEngine() {

        return currentLockEngine;
    }

    public LockEngine getLockEngine() {
        return lockEngine;
    }

    public void setLockEngine(String lockEngine) {
        this.lockEngine = LockEngine.valueOf(lockEngine.trim());
    }

    public void setCurrentLockEngine(Lock currentLockEngine) {
        this.currentLockEngine = currentLockEngine;
    }

    public long getSpinIntervalTime() {
        return spinIntervalTime;
    }

    public void setSpinIntervalTime(long spinIntervalTime) {
        this.spinIntervalTime = spinIntervalTime;
    }

    public String getLeaderLockName() {
        return leaderLockName;
    }

    public void setLeaderLockName(String leaderLockName) {
        this.leaderLockName = leaderLockName;
    }

    public String getLockNamePrefix() {
        return lockNamePrefix;
    }

    public void setLockNamePrefix(String lockNamePrefix) {
        this.lockNamePrefix = lockNamePrefix;
    }

    public String getLockNameSuffix() {
        return lockNameSuffix;
    }

    public void setLockNameSuffix(String lockNameSuffix) {
        this.lockNameSuffix = lockNameSuffix;
    }

    public List<JedisPoolInfo> getJedisPools() {
        return jedisPools;
    }

    public void setJedisPools(List<JedisPoolInfo> jedisPools) {
        this.jedisPools = jedisPools;
    }

    public long getLockLeaseTime() {
        return lockLeaseTime;
    }

    public void setLockLeaseTime(long lockLeaseTime) {
        this.lockLeaseTime = lockLeaseTime;
    }

    public long getLockRenewIntervalTime() {
        return lockRenewIntervalTime;
    }

    public void setLockRenewIntervalTime(long lockRenewIntervalTime) {
        this.lockRenewIntervalTime = lockRenewIntervalTime;
    }

    public int getLockRenewExecutorCoreSize() {
        return lockRenewExecutorCoreSize;
    }

    public void setLockRenewExecutorCoreSize(int lockRenewExecutorCoreSize) {
        this.lockRenewExecutorCoreSize = lockRenewExecutorCoreSize;
    }

    public int getLockRenewExecutorMaxSize() {
        return lockRenewExecutorMaxSize;
    }

    public void setLockRenewExecutorMaxSize(int lockRenewExecutorMaxSize) {
        this.lockRenewExecutorMaxSize = lockRenewExecutorMaxSize;
    }

    public int getLockRenewExecutorQueueSize() {
        return lockRenewExecutorQueueSize;
    }

    public void setLockRenewExecutorQueueSize(int lockRenewExecutorQueueSize) {
        this.lockRenewExecutorQueueSize = lockRenewExecutorQueueSize;
    }
}
