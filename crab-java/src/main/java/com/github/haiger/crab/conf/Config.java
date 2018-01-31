package com.github.haiger.crab.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Haiger
 * @version $Id: Config.java, v 0.1 2018-01-12 10:42:57 Haiger Exp $
 */
public class Config {
    private static final Logger logger = LoggerFactory.getLogger(Config.class);

    private static final String ENGINE             = "crab.lock.engine";
    private static final String SPIN_INTERVAL_TIME = "crab.lock.spin.interval-time";
    private static final String LEADER_NAME        = "crab.lock.leader-name";
    private static final String NAME_PREFIX        = "crab.lock.name-prefix";
    private static final String NAME_SUFFIX        = "crab.lock.name-suffix";
    private static final String LEASE_TIME         = "crab.lock.lease-time";

    private static final String RENEW_INTERVAL_TIME       = "crab.lock.renew.interval-time";
    private static final String RENEW_EXECUTOR_CORE_SIZE  = "crab.lock.renew.executor.core-size";
    private static final String RENEW_EXECUTOR_MAX_SIZE   = "crab.lock.renew.executor.max-size";
    private static final String RENEW_EXECUTOR_QUEUE_SIZE = "crab.lock.renew.executor.queue-size";

    private static final String DEFAULT_RENEW_CORE_SIZE  = "10";
    private static final String DEFAULT_RENEW_MAX_SIZE   = "20";
    private static final String DEFAULT_RENEW_QUEUE_SIZE = "1000";
    private static final String DEFAULT_LEADER_NAME      = "LEADER";

    private String engine;
    private long   spinIntervalTime;
    private String leaderName;
    private String namePrefix;
    private String nameSuffix;
    private long   leaseTime;
    private long   renewIntervalTime;

    private int renewExecutorCoreSize;
    private int renewExecutorMaxSize;
    private int renewExecutorQueueSize;

    // redis config
    private RedisConfig redisConfig;

    private Config() {
    }

    public Config load(String filePath) throws Throwable {
        Properties props = new Properties();
        props.load(Config.class.getResourceAsStream(filePath));

        return loadWithProps(props);
    }

    public Config loadWithFile(File file) throws IOException {
        Properties props = new Properties();

        try (FileInputStream fileInput = new FileInputStream(file)) {
            props.load(fileInput);
        }

        return loadWithProps(props);
    }

    public Config loadWithProps(Properties props) {
        this.engine = props.getProperty(ENGINE, LockEngine.LOCAL.name());
        this.spinIntervalTime = Long.valueOf(props.getProperty(SPIN_INTERVAL_TIME));
        this.leaderName = props.getProperty(LEADER_NAME, DEFAULT_LEADER_NAME);
        this.namePrefix = props.getProperty(NAME_PREFIX);
        this.nameSuffix = props.getProperty(NAME_SUFFIX);
        this.leaseTime = Long.valueOf(props.getProperty(LEASE_TIME));
        this.renewIntervalTime = Long.valueOf(props.getProperty(RENEW_INTERVAL_TIME));
        this.renewExecutorCoreSize = Integer.valueOf(props.getProperty(RENEW_EXECUTOR_CORE_SIZE, DEFAULT_RENEW_CORE_SIZE));
        this.renewExecutorMaxSize = Integer.valueOf(props.getProperty(RENEW_EXECUTOR_MAX_SIZE, DEFAULT_RENEW_MAX_SIZE));
        this.renewExecutorQueueSize = Integer.valueOf(props.getProperty(RENEW_EXECUTOR_QUEUE_SIZE, DEFAULT_RENEW_QUEUE_SIZE));

        if (engine.equalsIgnoreCase(LockEngine.REDIS.name())) {
            this.redisConfig = new RedisConfig(props);
        }

        logger.info("crab config info:{}", this.toString());
        return this;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Config [");
        sb.append("engine=").append(engine);
        sb.append(", spinIntervalTime=").append(spinIntervalTime);
        sb.append(", leaderName=").append(leaderName);
        sb.append(", namePrefix=").append(namePrefix);
        sb.append(", nameSuffix=").append(nameSuffix);
        sb.append(", leaseTime=").append(leaseTime);
        sb.append(", renewIntervalTime=").append(renewIntervalTime);
        sb.append(", renewExecutorCoreSize=").append(renewExecutorCoreSize);
        sb.append(", renewExecutorMaxSize=").append(renewExecutorMaxSize);
        sb.append(", renewExecutorQueueSize=").append(renewExecutorQueueSize);
        sb.append(", redisConfig=").append(redisConfig);
        sb.append(']');
        return sb.toString();
    }

    public static Config getInstance() {
        return ConfigHolder.instance;
    }

    private static class ConfigHolder {
        static final Config instance = new Config();
    }

    public String getEngine() {
        return engine;
    }

    public Config setEngine(String engine) {
        this.engine = engine;
        return this;
    }

    public long getSpinIntervalTime() {
        return spinIntervalTime;
    }

    public Config setSpinIntervalTime(long spinIntervalTime) {
        this.spinIntervalTime = spinIntervalTime;
        return this;
    }

    public String getLeaderName() {
        return leaderName;
    }

    public Config setLeaderName(String leaderName) {
        this.leaderName = leaderName;
        return this;
    }

    public String getNamePrefix() {
        return namePrefix;
    }

    public Config setNamePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
        return this;
    }

    public String getNameSuffix() {
        return nameSuffix;
    }

    public Config setNameSuffix(String nameSuffix) {
        this.nameSuffix = nameSuffix;
        return this;
    }

    public long getLeaseTime() {
        return leaseTime;
    }

    public Config setLeaseTime(long leaseTime) {
        this.leaseTime = leaseTime;
        return this;
    }

    public long getRenewIntervalTime() {
        return renewIntervalTime;
    }

    public Config setRenewIntervalTime(long renewIntervalTime) {
        this.renewIntervalTime = renewIntervalTime;
        return this;
    }

    public int getRenewExecutorCoreSize() {
        return renewExecutorCoreSize;
    }

    public Config setRenewExecutorCoreSize(int renewExecutorCoreSize) {
        this.renewExecutorCoreSize = renewExecutorCoreSize;
        return this;
    }

    public int getRenewExecutorMaxSize() {
        return renewExecutorMaxSize;
    }

    public Config setRenewExecutorMaxSize(int renewExecutorMaxSize) {
        this.renewExecutorMaxSize = renewExecutorMaxSize;
        return this;
    }

    public int getRenewExecutorQueueSize() {
        return renewExecutorQueueSize;
    }

    public Config setRenewExecutorQueueSize(int renewExecutorQueueSize) {
        this.renewExecutorQueueSize = renewExecutorQueueSize;
        return this;
    }

    public RedisConfig getRedisConfig() {
        return redisConfig;
    }

    public Config setRedisConfig(RedisConfig redisConfig) {
        this.redisConfig = redisConfig;
        return this;
    }
}
