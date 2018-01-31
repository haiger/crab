package com.github.haiger.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Haiger
 * @version $Id: CrabProperties.java, v 0.1 2018-01-12 17:02:42 Haiger Exp $
 */
@ConfigurationProperties(prefix = "crab")
public class CrabProperties {
    private boolean leaderEnable;
    private String leaderName = "LEADER";

    private String lockEngine;

    private Lock lock;

    public static class Lock {
        private long   spinIntervalTime;
        private String namePrefix;
        private String nameSuffix;
        private long   leaseTime;
        private long   renewIntervalTime;
        private int    renewExecutorCoreSize;
        private int    renewExecutorMaxSize;
        private int    renewExecutorQueueSize;

        private RedisEngine redisEngine;

        public long getSpinIntervalTime() {
            return spinIntervalTime;
        }

        public void setSpinIntervalTime(long spinIntervalTime) {
            this.spinIntervalTime = spinIntervalTime;
        }

        public String getNamePrefix() {
            return namePrefix;
        }

        public void setNamePrefix(String namePrefix) {
            this.namePrefix = namePrefix;
        }

        public String getNameSuffix() {
            return nameSuffix;
        }

        public void setNameSuffix(String nameSuffix) {
            this.nameSuffix = nameSuffix;
        }

        public long getLeaseTime() {
            return leaseTime;
        }

        public void setLeaseTime(long leaseTime) {
            this.leaseTime = leaseTime;
        }

        public long getRenewIntervalTime() {
            return renewIntervalTime;
        }

        public void setRenewIntervalTime(long renewIntervalTime) {
            this.renewIntervalTime = renewIntervalTime;
        }

        public int getRenewExecutorCoreSize() {
            return renewExecutorCoreSize;
        }

        public void setRenewExecutorCoreSize(int renewExecutorCoreSize) {
            this.renewExecutorCoreSize = renewExecutorCoreSize;
        }

        public int getRenewExecutorMaxSize() {
            return renewExecutorMaxSize;
        }

        public void setRenewExecutorMaxSize(int renewExecutorMaxSize) {
            this.renewExecutorMaxSize = renewExecutorMaxSize;
        }

        public int getRenewExecutorQueueSize() {
            return renewExecutorQueueSize;
        }

        public void setRenewExecutorQueueSize(int renewExecutorQueueSize) {
            this.renewExecutorQueueSize = renewExecutorQueueSize;
        }

        public RedisEngine getRedisEngine() {
            return redisEngine;
        }

        public void setRedisEngine(RedisEngine redisEngine) {
            this.redisEngine = redisEngine;
        }
    }

    public static class RedisEngine {
        private int    database;
        private String password;
        private String nodes;
        private int    connectionTimeout;
        private int    readTimeout;
        private Pool   pool;

        public int getDatabase() {
            return database;
        }

        public void setDatabase(int database) {
            this.database = database;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getNodes() {
            return nodes;
        }

        public void setNodes(String nodes) {
            this.nodes = nodes;
        }

        public int getConnectionTimeout() {
            return connectionTimeout;
        }

        public void setConnectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }

        public int getReadTimeout() {
            return readTimeout;
        }

        public void setReadTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
        }

        public Pool getPool() {
            return pool;
        }

        public void setPool(Pool pool) {
            this.pool = pool;
        }
    }

    public static class Pool {
        private int     maxIdle      = 5;
        private int     minIdle      = 3;
        private int     maxActive    = 8;
        private int     maxWait      = 1000;
        private boolean testOnBorrow = true;

        public int getMaxIdle() {
            return maxIdle;
        }

        public void setMaxIdle(int maxIdle) {
            this.maxIdle = maxIdle;
        }

        public int getMinIdle() {
            return minIdle;
        }

        public void setMinIdle(int minIdle) {
            this.minIdle = minIdle;
        }

        public int getMaxActive() {
            return maxActive;
        }

        public void setMaxActive(int maxActive) {
            this.maxActive = maxActive;
        }

        public int getMaxWait() {
            return maxWait;
        }

        public void setMaxWait(int maxWait) {
            this.maxWait = maxWait;
        }

        public boolean isTestOnBorrow() {
            return testOnBorrow;
        }

        public void setTestOnBorrow(boolean testOnBorrow) {
            this.testOnBorrow = testOnBorrow;
        }
    }

    public boolean isLeaderEnable() {
        return leaderEnable;
    }

    public void setLeaderEnable(boolean leaderEnable) {
        this.leaderEnable = leaderEnable;
    }

    public String getLeaderName() {
        return leaderName;
    }

    public void setLeaderName(String leaderName) {
        this.leaderName = leaderName;
    }

    public String getLockEngine() {
        return lockEngine;
    }

    public void setLockEngine(String lockEngine) {
        this.lockEngine = lockEngine;
    }

    public Lock getLock() {
        return lock;
    }

    public void setLock(Lock lock) {
        this.lock = lock;
    }
}
