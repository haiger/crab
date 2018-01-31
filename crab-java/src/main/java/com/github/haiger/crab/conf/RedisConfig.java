package com.github.haiger.crab.conf;

import java.util.Properties;

/**
 * @author Haiger
 * @version $Id: RedisConfig.java, v 0.1 2018-01-12 12:12:39 Haiger Exp $
 */
public class RedisConfig {
    private static final String DATABASE            = "crab.lock.engine.redis.database";
    private static final String PASSWORD            = "crab.lock.engine.redis.password";
    private static final String NODES               = "crab.lock.engine.redis.nodes";
    private static final String CONNECTION_TIMEOUT  = "crab.lock.engine.redis.connection-timeout";
    private static final String READ_TIMEOUT        = "crab.lock.engine.redis.read-timeout";
    private static final String POOL_MAX_ACTIVE     = "crab.lock.engine.redis.pool.max-active";
    private static final String POOL_MAX_IDLE       = "crab.lock.engine.redis.pool.max-idle";
    private static final String POOL_MIN_IDLE       = "crab.lock.engine.redis.pool.min-idle";
    private static final String POOL_MAX_WAIT       = "crab.lock.engine.redis.pool.max-wait";
    private static final String POOL_TEST_ON_BORROW = "crab.lock.engine.redis.pool.test-on-borrow";

    private static final String DEFAULT_DATABASE = "0";

    private int     database;
    private String  password;
    private String  nodes;
    private int     connectionTimeout;
    private int     readTimeout;
    private int     poolMaxActive;
    private int     poolMaxIdle;
    private int     poolMinIdle;
    private int     poolMaxWait;
    private boolean poolTestOnBorrow;

    public RedisConfig() {
    }

    public RedisConfig(Properties props) {
        this.database = Integer.valueOf(props.getProperty(DATABASE, DEFAULT_DATABASE));
        this.password = props.getProperty(PASSWORD);
        this.nodes = props.getProperty(NODES);
        this.connectionTimeout = Integer.valueOf(props.getProperty(CONNECTION_TIMEOUT));
        this.readTimeout = Integer.valueOf(props.getProperty(READ_TIMEOUT));
        this.poolMaxActive = Integer.valueOf(props.getProperty(POOL_MAX_ACTIVE));
        this.poolMaxIdle = Integer.valueOf(props.getProperty(POOL_MAX_IDLE));
        this.poolMinIdle = Integer.valueOf(props.getProperty(POOL_MIN_IDLE));
        this.poolMaxWait = Integer.valueOf(props.getProperty(POOL_MAX_WAIT));
        this.poolTestOnBorrow = Boolean.valueOf(props.getProperty(POOL_TEST_ON_BORROW));
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("RedisConfig [");
        sb.append("database=").append(database);
        sb.append(", password=").append(password);
        sb.append(", nodes=").append(nodes);
        sb.append(", connectionTimeout=").append(connectionTimeout);
        sb.append(", readTimeout=").append(readTimeout);
        sb.append(", poolMaxActive=").append(poolMaxActive);
        sb.append(", poolMaxIdle=").append(poolMaxIdle);
        sb.append(", poolMinIdle=").append(poolMinIdle);
        sb.append(", poolMaxWait=").append(poolMaxWait);
        sb.append(", poolTestOnBorrow=").append(poolTestOnBorrow);
        sb.append(']');
        return sb.toString();
    }

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

    public int getPoolMaxActive() {
        return poolMaxActive;
    }

    public void setPoolMaxActive(int poolMaxActive) {
        this.poolMaxActive = poolMaxActive;
    }

    public int getPoolMaxIdle() {
        return poolMaxIdle;
    }

    public void setPoolMaxIdle(int poolMaxIdle) {
        this.poolMaxIdle = poolMaxIdle;
    }

    public int getPoolMinIdle() {
        return poolMinIdle;
    }

    public void setPoolMinIdle(int poolMinIdle) {
        this.poolMinIdle = poolMinIdle;
    }

    public int getPoolMaxWait() {
        return poolMaxWait;
    }

    public void setPoolMaxWait(int poolMaxWait) {
        this.poolMaxWait = poolMaxWait;
    }

    public boolean isPoolTestOnBorrow() {
        return poolTestOnBorrow;
    }

    public void setPoolTestOnBorrow(boolean poolTestOnBorrow) {
        this.poolTestOnBorrow = poolTestOnBorrow;
    }
}
